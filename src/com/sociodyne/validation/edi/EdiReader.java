package com.sociodyne.validation.edi;

import com.sociodyne.LockingHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Writes the EDI contents into a simple XML representation. The parser emits
 * the EDI elements into the default namespace. This class worries only about
 * syntax. Validation and semantics are handled at a higher layer.
 * <p>
 * By default, this implementation wraps almost all {@code Exceptions} in the
 * same type of exception with the {@code cause} set to the original exception
 * (if the exception's constructor supports it). The wrapped exception's message
 * will indicate where in the input the parsing failed. This feature can be
 * disabled through the use of the feature
 * {@code http://www.sociodyne.com/xmlns/xml/wrapExceptions}.
 * <p>
 * For MT safety, wrap the setting of the separators with the parse operation. The parse operation
 * compartmentalizes the configuration.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
// TODO(jkinner): Loops (LS/LE and unbounded) and HL blocks
public class EdiReader implements XMLReader, EdiConstants {

	/** Feature URI for wrapping exceptions with location information. */
	public static final String WRAP_EXCEPTIONS_FEATURE =
		"http://www.sociodyne.com/xmlns/xml/wrapExceptions";

	/** SAX namespace feature URI. */
	private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

	/** SAX namespace prefix feature URI. */
	private static final String NAMESPACE_PREFIXES_FEATURE =
		"http://xml.org/sax/features/namespace-prefixes";
	
	// TODO(jkinner): Implement validation
	/** SAX validation feature URI. */
	private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";

	/** An empty {@code Attributes} object. Used throughout the package as a singleton. */
	static final Attributes EMPTY_ATTRIBUTES = new EdiAttributes();

	/** Configured segment terminator. */
	private char segmentTerminator = '~';

	/** Configured element separator. */
	private char elementSeparator = '*';

	/** Configured sub-element separator. */
	private char subElementSeparator = '|';

	// XMLReader fields
	private ContentHandler contentHandler;
	private DTDHandler dtdHandler;
	private EntityResolver entityResolver;
	private ErrorHandler errorHandler;

	/** Value of the {@link #WRAP_EXCEPTIONS_FEATURE} feature. */
	private boolean wrapExceptions = true;

	/** Value of the {@link #NAMESPACE_FEATURE} feature. */
	private boolean useNamespaces = true;

	/** Value of the {@link #NAMESPACE_PREFIXES_FEATURE} feature. */
	private boolean useNamespacePrefixes = false;

	/** For a given EDI element, what its well-known identifier is. */
	private static final Map<ImmutableEdiLocation, String> wellKnownElements = ImmutableMap
		.<ImmutableEdiLocation, String> builder()
			.put(ImmutableEdiLocation.of("ISA", 2), "authorizationInformation")
			.put(ImmutableEdiLocation.of("ISA", 3), "securityInformationQualifier")
			.put(ImmutableEdiLocation.of("ISA", 5), "interchangeSenderIdQualifier")
			.put(ImmutableEdiLocation.of("ISA", 6), "interchangeSenderId")
			.put(ImmutableEdiLocation.of("ISA", 7), "interchangeReceiverIdQualifier")
			.put(ImmutableEdiLocation.of("ISA", 8), "interchangeReceiverId")
			.put(ImmutableEdiLocation.of("ISA", 14), "acknowledgementRequested")
			.put(ImmutableEdiLocation.of("ISA", 16), "subElementSeparator").build();

	private final ParserFactory<SegmentParser> segmentParserFactory;

	@Inject
	EdiReader(ParserFactory<SegmentParser> segmentParserFactory) {
		this.segmentParserFactory = segmentParserFactory;
	}

	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	public DTDHandler getDTDHandler() {
		return dtdHandler;
	}

	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public boolean getFeature(String feature) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		if (feature.equals(NAMESPACES_FEATURE)) {
			return useNamespaces;
		}
		if (feature.equals(NAMESPACE_PREFIXES_FEATURE)) {
			return false;
		}
		if (feature.equals(WRAP_EXCEPTIONS_FEATURE)) {
			return wrapExceptions;
		}

		if (feature.startsWith("http://xml.org/sax/")) {
			throw new SAXNotSupportedException(feature);
		} else {
			throw new SAXNotRecognizedException(feature);
		}
	}

	public Object getProperty(String property)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		// TODO(jkinner): Decide what properties to expose
		throw new SAXNotSupportedException();
	}

	public void parse(InputSource inputSource) throws IOException, SAXException {
		Reader characterStream = inputSource.getCharacterStream();
		if (characterStream != null) {
			parseReader(characterStream);
		} else if (inputSource.getByteStream() != null) {
			parseReader(new InputStreamReader(inputSource.getByteStream()));
		}
	}

	public void parse(String url) throws IOException, SAXException {
		InputStream is = new URL(url).openStream();
		Reader r = new InputStreamReader(is, "UTF-8");
		parseReader(r);
	}

	protected void parseReader(Reader reader) throws IOException, SAXException {
		Configuration configuration;
		Location location = new Location();
		location.nextLine();

		configuration = new Configuration.Builder()
				.setElementSeparator(elementSeparator)
				.setSegmentTerminator(segmentTerminator)
				.setSubElementSeparator(subElementSeparator).build();

		contentHandler.startDocument();
		contentHandler.startPrefixMapping("", NAMESPACE_URI);

		// TODO(jkinner): Fill in ASC X12 version info?
		contentHandler.startElement(NAMESPACE_URI, "edi", "edi",
				EMPTY_ATTRIBUTES);
		
		SegmentParser segmentParser = segmentParserFactory.create(reader, configuration,
				contentHandler, location);

		// TODO(jkinner): Use worker threads to do parsing; from here down, the operations are
		// thread-safe.
		try {
			// Simple recursive-descent parser
			segmentParser.parse();
		} catch (SAXException e) {
			throw wrapThrowable(e, location);
		} catch (IOException e) {
			throw wrapThrowable(e, location);
		} catch (AssertionError e) {
			// For JUnit assertions
			throw wrapThrowable(e, location);
		} catch (Throwable t) {
			throw new RuntimeException(wrapThrowable(t, location));
		}

		contentHandler.endElement(NAMESPACE_URI, "edi", "edi");

		contentHandler.endDocument();
	}

	protected boolean isLoopSegment(String segmentIdentifier) {
		if (segmentIdentifier == "EB") {
			return true;
		}

		return false;
	}

	public void setContentHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	public void setDTDHandler(DTDHandler dtdHandler) {
		this.dtdHandler = dtdHandler;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void setFeature(String feature, boolean enabled)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		if (feature.equals(NAMESPACES_FEATURE)) {
			useNamespaces = enabled;
		}
		if (feature.equals(NAMESPACE_PREFIXES_FEATURE)) {
			throw new SAXNotSupportedException(feature);
		}
		if (feature.equals(WRAP_EXCEPTIONS_FEATURE)) {
			wrapExceptions = enabled;
		}

		if (feature.startsWith("http://xml.org/sax/")) {
			throw new SAXNotSupportedException(feature);
		} else {
			throw new SAXNotRecognizedException(feature);
		}
	}

	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException();
	}

	@VisibleForTesting
	static class EdiAttributes extends LinkedHashMap<QName, String> implements
			Attributes {
		private static final long serialVersionUID = 5189202684533412495L;
		private static final String CDATA_TYPE = "CDATA";

		public int getIndex(String qName) {
			int index = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (qName.equals(renderPrefix(attributeQName)
						+ attributeQName.getLocalPart())) {
					return index;
				}
				index++;
			}

			return -1;
		}

		public int getIndex(String uri, String localName) {
			int index = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (uri.equals(attributeQName.getNamespaceURI())
						&& localName.equals(attributeQName.getLocalPart())) {
					return index;
				}
				index++;
			}

			return -1;
		}

		public int getLength() {
			return size();
		}

		public String getLocalName(int index) {
			int count = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (count == index) {
					return get(attributeQName);
				}
				count++;
			}

			return null;
		}

		public String getQName(int index) {
			int count = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (count == index) {
					return renderPrefix(attributeQName)
							+ attributeQName.getLocalPart();
				}
				count++;
			}

			return null;
		}

		public String getType(int index) {
			return CDATA_TYPE;
		}

		public String getType(String qName) {
			return CDATA_TYPE;
		}

		public String getType(String uri, String localName) {
			return CDATA_TYPE;
		}

		public String getURI(int index) {
			return getEntryForIndex(index).getKey().getNamespaceURI();
		}

		public String getValue(int index) {
			return getEntryForIndex(index).getValue();
		}

		public String getValue(String qName) {
			Map.Entry<QName, String> entry = getEntryForQName(qName);
			return entry == null ? null : entry.getValue();
		}

		public String getValue(String uri, String localName) {
			Map.Entry<QName, String> entry = getEntryForQNameParts(uri,
					localName);
			return entry == null ? null : entry.getValue();
		}

		private String renderPrefix(QName qname) {
			if (qname.getPrefix() != null && qname.getPrefix().length() > 0) {
				return qname.getPrefix() + ":";
			}

			return "";
		}

		private Map.Entry<QName, String> getEntryForIndex(int index) {
			Preconditions.checkPositionIndex(index, size());

			int count = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				if (count == index) {
					return attributeEntry;
				}
				count++;
			}

			throw new ArrayIndexOutOfBoundsException(index);

		}

		private Map.Entry<QName, String> getEntryForQName(String qName) {
			int index = 0;
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (qName.equals(renderPrefix(attributeQName)
						+ attributeQName.getLocalPart())) {
					return attributeEntry;
				}
				index++;
			}

			return null;
		}

		private Map.Entry<QName, String> getEntryForQNameParts(String uri,
				String localName) {
			for (Map.Entry<QName, String> attributeEntry : entrySet()) {
				QName attributeQName = attributeEntry.getKey();
				if (uri.equals(attributeQName.getNamespaceURI())
						&& localName.equals(attributeQName.getLocalPart())) {
					return attributeEntry;
				}
			}

			return null;
		}

		// NOTE: These implementations are horribly inefficient; they are used for testing.
		public boolean equals(Object o) {
			// TODO(jkinner): Should order count?
			if (this == o) { return true; }
			if (o instanceof EdiAttributes) {
				EdiAttributes that = (EdiAttributes) o;
				if (size() == that.size()) {
					// Given that the sizes are equal, check each entry in this map with the other
					for (Map.Entry<QName, String> entry : entrySet()) {
						String value = entry.getValue();
						if (! Objects.equal(value, that.get(entry.getKey()))) {
							return false;
						}
					}

					// Every entry has been verified
					return true;
				}
			}

			return false;
		}

		public int hashCode() {
			Object[] hashCodeParams = new Object[size() * 2];
			int i = 0;
			for (Map.Entry<QName, String> entry : entrySet()) {
				hashCodeParams[i] = entry.getKey();
				hashCodeParams[i+1] = entry.getValue();
				i += 2;
			}
			return Objects.hashCode(hashCodeParams);
		}
	}

	public static class Configuration {
		private char segmentTerminator = '~';
		private char elementSeparator = '*';
		private LockingHolder<Character> subElementSeparator = LockingHolder
				.of('|');

		public static class Builder {
			private Character segmentTerminator;
			private Character elementSeparator;
			private Character subElementSeparator;

			public Builder setSegmentTerminator(char segmentTerminator) {
				this.segmentTerminator = segmentTerminator;
				return this;
			}

			public Builder setElementSeparator(char elementSeparator) {
				this.elementSeparator = elementSeparator;
				return this;
			}

			public Builder setSubElementSeparator(char subElementSeparator) {
				this.subElementSeparator = subElementSeparator;
				return this;
			}

			public Configuration build() {
				Configuration configuration = new Configuration();
				if (segmentTerminator != null) {
					configuration.segmentTerminator = segmentTerminator;
				}
				if (elementSeparator != null) {
					configuration.elementSeparator = elementSeparator;
				}
				if (subElementSeparator != null) {
					configuration.subElementSeparator.set(subElementSeparator);
				}

				return configuration;
			}
		}

		public char getSubElementSeparator() {
			return subElementSeparator.get();
		}

		public void setSubElementSeparator(Character subElementSeparator) {
			// This is only allowed to change once during parsing: in the ISA
			// block.
			this.subElementSeparator.set(subElementSeparator);
			this.subElementSeparator.lock();
		}

		public char getSegmentTerminator() {
			return segmentTerminator;
		}

		public char getElementSeparator() {
			return elementSeparator;
		}
	}

	public static class Location {
		private int line;
		private int character;
		// This is always where we start parsing EDI
		private EdiLocation elementLocation = new EdiLocation("ISA", 0);

		public Location() {
		}

		public void nextLine() {
			line++;
			character = 0;
		}

		public void nextChar() {
			character++;
		}

		public void startSegment(String segment) {
			elementLocation.setSegment(segment);
			elementLocation.setElement(0);
			elementLocation.clearSubElement();
		}

		public void startElement() {
			elementLocation.nextElement();
		}

		public void endElement() {
			elementLocation.clearSubElement();
		}

		public void startSubElement() {
			if (elementLocation.hasSubElement()) {
				elementLocation.nextSubElement();
			} else {
				elementLocation.setSubElement(0);
			}
		}

		public ImmutableEdiLocation getEdiLocation() {
			return elementLocation;
		}

		@Override
		public String toString() {
			StringBuffer locationBuffer = new StringBuffer();
			locationBuffer.append("line ").append(line).append(", character ")
					.append(character).append(", segment ")
					.append(elementLocation.getSegment());
			if (elementLocation.getIndex() > 0) {
				locationBuffer.append(", element ")
						.append(elementLocation.getIndex());
			}
			if (elementLocation.hasSubElement()) {
				locationBuffer.append("subelement ").append(
						elementLocation.getSubElement());
			}
			String wellKnownName = wellKnownElements.get(elementLocation);
			if (wellKnownName != null) {
				locationBuffer.append(" ('").append(wellKnownName).append("')");
			}
			return locationBuffer.toString();
		}

	}

	private <T extends Throwable> T wrapThrowable(T throwable, Location location) {
		if (!wrapExceptions) {
			return throwable;
		}

		T wrapped = null;

		try {
			// Type-safe multivariate cast
			@SuppressWarnings("unchecked")
			Constructor<T> c = (Constructor<T>) throwable.getClass()
					.getConstructor(
							new Class<?>[] { String.class, Throwable.class });
			wrapped = (T) c.newInstance("At " + location.toString(), throwable);
		} catch (NoSuchMethodException e) {
			// Try the Exception ctor
			if (Exception.class.isAssignableFrom(throwable.getClass())) {
				try {
					// Type-safe multivariate cast
					@SuppressWarnings("unchecked")
					Constructor<T> c = (Constructor<T>) throwable.getClass()
							.getConstructor(
									new Class<?>[] { String.class,
											Exception.class });
					wrapped = (T) c.newInstance("At " + location.toString(),
							throwable);
				} catch (Exception e2) {
					// Look for a String ctor and at least create the new
					// message
				}
			}
		} catch (Exception e) {
			// Do nothing; there are bigger issues than not being able to wrap
			// the exception
		}
		if (wrapped == null) {
			try {
				@SuppressWarnings("unchecked")
				Constructor<T> c = (Constructor<T>) throwable.getClass()
						.getConstructor(new Class<?>[] { String.class });
				wrapped = (T) c.newInstance("At " + location.toString() + ": "
						+ throwable.getMessage());
			} catch (Exception e3) {
				// Maybe it will accept and Object (Like AssertionError)
				try {
					@SuppressWarnings("unchecked")
					Constructor<T> c = (Constructor<T>) throwable.getClass()
							.getConstructor(new Class<?>[] { Object.class });
					wrapped = (T) c.newInstance("At " + location.toString()
							+ ": " + throwable.getMessage());
				} catch (Exception e) {
					// Give up.
				}
			}
		}

		return wrapped == null ? throwable : wrapped;
	}

	public static class Factory {
		private static Injector ediReaderInjector = Guice.createInjector(new EdiReaderModule());

		public static EdiReader create() {
			return ediReaderInjector.getInstance(EdiReader.class);
		}
	}
}
