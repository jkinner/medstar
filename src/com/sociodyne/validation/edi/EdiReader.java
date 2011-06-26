package com.sociodyne.validation.edi;

import com.sociodyne.LockingHolder;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

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
public class EdiReader implements XMLReader {

	public static final String NAMESPACE_URI = "http://www.sociodyne.com/xmlns/edi";
	public static final String WRAP_EXCEPTIONS_FEATURE = "http://www.sociodyne.com/xmlns/xml/wrapExceptions";
	private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
	private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
	// TODO(jkinner): Implement validation
	private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";

	private static final String SUBELEMENT_ELEMENT = "subelement";
	private static final String ELEMENT_ELEMENT = "element";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final ImmutableEdiElementIdentifier ISA_SUBELEMENT_IDENTIFIER = ImmutableEdiElementIdentifier
			.of("ISA", 16);
	private static final String SEGMENT_ELEMENT = "segment";
	private static final Attributes EMPTY_ATTRIBUTES = new EdiAttributes();

	private char segmentTerminator = '~';
	private char elementSeparator = '*';
	private char subElementSeparator = '|';

	// XMLReader fields
	private ContentHandler contentHandler;
	private DTDHandler dtdHandler;
	private EntityResolver entityResolver;
	private ErrorHandler errorHandler;

	private boolean wrapExceptions = true;
	private boolean useNamespaces = true;
	private boolean useNamespacePrefixes = false;

	private static final Map<ImmutableEdiElementIdentifier, ValueTransformer<String, String>> ediValueTransformers = ImmutableMap
			.of();

	private static final Map<ImmutableEdiElementIdentifier, String> wellKnownElements = ImmutableMap
			.<ImmutableEdiElementIdentifier, String> builder()
			.put(ImmutableEdiElementIdentifier.of("ISA", 2),
					"authorizationInformation")
			.put(ImmutableEdiElementIdentifier.of("ISA", 3),
					"securityInformationQualifier")
			.put(ImmutableEdiElementIdentifier.of("ISA", 5),
					"interchangeSenderIdQualifier")
			.put(ImmutableEdiElementIdentifier.of("ISA", 6),
					"interchangeSenderId")
			.put(ImmutableEdiElementIdentifier.of("ISA", 7),
					"interchangeReceiverIdQualifier")
			.put(ImmutableEdiElementIdentifier.of("ISA", 8),
					"interchangeReceiverId")
			.put(ImmutableEdiElementIdentifier.of("ISA", 14),
					"acknowledgementRequested")
			.put(ImmutableEdiElementIdentifier.of("ISA", 16),
					"subElementSeparator").build();

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
	}

	protected void parseReader(Reader is) throws IOException, SAXException {
		// Build a configuration in a thread-safe way so multiple threads can
		// use the same
		// parser instance.
		Configuration configuration;
		Location location = new Location();
		location.nextLine();

		configuration = new Configuration.Builder()
				.setElementSeparator(elementSeparator)
				.setSegmentTerminator(segmentTerminator)
				.setSubElementSeparator(subElementSeparator).build();

		contentHandler.startDocument();
		contentHandler.startPrefixMapping("", NAMESPACE_URI);

		// TODO(jkinner): Fill in ANSI X12 version info?
		contentHandler.startElement(NAMESPACE_URI, "edi", "edi",
				EMPTY_ATTRIBUTES);
		try {
			// Simple recursive-descent parser
			parseSegments(is, configuration, location);
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

	protected void parseSegments(Reader is, Configuration configuration,
			Location location) throws IOException, SAXException {
		StringBuffer accumulator = new StringBuffer();
		String segmentIdentifier = null;

		// TODO(jkinner): Auto-detect configuration while parsing segments
		// (separators)
		char elementSeparator = configuration.getElementSeparator();
		int read;
		Character ch = null;
		while ((read = is.read()) != -1) {
			ch = (char) read;

			if (ch == '\n') {
				location.nextLine();
			} else {
				location.nextChar();
			}

			if (ch == elementSeparator) {
				if (segmentIdentifier == null) {
					segmentIdentifier = accumulator.toString();

					startSegment(segmentIdentifier, location);

					// Now we recurse into the segment parsing
					ch = parseElements(segmentIdentifier, is, configuration,
							location);
					endSegment(segmentIdentifier);
					segmentIdentifier = null;

					blank(accumulator);

					continue;
				}
			} else if (ch == segmentTerminator) {
				if (accumulator.length() > 0) {
					segmentIdentifier = accumulator.toString();
					startSegment(segmentIdentifier, location);
					endSegment(segmentIdentifier);
					segmentIdentifier = null;

					continue;
				}
			}

			accumulator.append(ch);
		}

		if (ch != null && ch != segmentTerminator && segmentIdentifier == null) {
			throw new EOFException("Unexpected EOF when parsing segment "
					+ " (missing segment terminator '" + segmentTerminator
					+ "')");
		}
	}

	private void endSegment(String segmentIdentifier) throws SAXException {
		// The whole segment is finished. Send the "end" event.
		contentHandler.endElement(NAMESPACE_URI, SEGMENT_ELEMENT,
				SEGMENT_ELEMENT);
	}

	private void startSegment(String segmentIdentifier, Location location)
			throws SAXException {
		location.startSegment(segmentIdentifier);
		EdiAttributes attributes = new EdiAttributes();
		attributes.put(new QName("", TYPE_ATTRIBUTE, ""), segmentIdentifier);

		// TODO(jkinner): Fill in attributes
		// We are placing elements in the default namespace, so the qName ==
		// localPart
		contentHandler.startElement(NAMESPACE_URI, SEGMENT_ELEMENT,
				SEGMENT_ELEMENT, attributes);
	}

	protected char parseElements(String segmentIdentifier, Reader reader,
			Configuration configuration, Location location) throws IOException,
			SAXException {
		StringBuffer accumulator = new StringBuffer();

		int read;

		char segmentTerminator = configuration.getSegmentTerminator();
		char elementSeparator = configuration.getElementSeparator();
		char subElementSeparator = configuration.getSubElementSeparator();

		while ((read = reader.read()) != -1) {
			char ch = (char) read;
			if (ch == '\n') {
				location.nextLine();
			} else {
				location.nextChar();
			}

			if (ch == elementSeparator || ch == segmentTerminator) {
				startElement(segmentIdentifier, accumulator, location);
				endElement(segmentIdentifier);

				if (location.getElementIdentifier().equals(
						ISA_SUBELEMENT_IDENTIFIER)) {
					configuration.setSubElementSeparator(accumulator.charAt(0));
				}

				if (ch == segmentTerminator) {
					// The caller will close the segment
					return ch;
				}

				blank(accumulator);

				continue;
			}

			// TODO(jkinner): Make this a flag after ISA is parsed instead of
			// string comparison
			if (!segmentIdentifier.equals("ISA") && ch == subElementSeparator) {
				// Parse subElements until the end of the element
				location.startElement();
				startElement(segmentIdentifier, accumulator, location);
				blank(accumulator);
				char terminatorToken = parseSubElements(reader, configuration,
						location);
				endElement(segmentIdentifier);
				location.endElement();

				if (terminatorToken == segmentTerminator) {
					return terminatorToken;
				}

				continue;
			}

			accumulator.append(ch);
		}

		throw new EOFException("Unexpected EOF when parsing element: "
				+ accumulator.toString() + " (missing segment terminator '"
				+ segmentTerminator + "' or element separator '"
				+ elementSeparator + "')");
	}

	private void blank(StringBuffer accumulator) {
		accumulator.delete(0, accumulator.length());
	}

	private void startElement(String segmentIdentifier,
			StringBuffer accumulator, Location location)
			throws SAXException {
		location.startElement();
		contentHandler.startElement(NAMESPACE_URI, ELEMENT_ELEMENT,
				ELEMENT_ELEMENT, EMPTY_ATTRIBUTES);

		ValueTransformer<String, String> transformer = ediValueTransformers
				.get(location.getElementIdentifier());
		String value = accumulator.toString();
		if (transformer != null) {
			value = transformer.transform(value);
		}

		char[] accumulatorChars = value.toCharArray();
		if (accumulatorChars.length > 0) {
			contentHandler.characters(accumulatorChars, 0,
					accumulatorChars.length);
		}
	}

	private void endElement(String segmentIdentifier) throws SAXException {
		contentHandler.endElement(NAMESPACE_URI, ELEMENT_ELEMENT,
				ELEMENT_ELEMENT);
	}

	protected char parseSubElements(Reader is, Configuration configuration,
			Location location) throws IOException, SAXException {
		StringBuffer accumulator = new StringBuffer();

		int read;
		int fieldCount = 0;

		char elementSeparator = configuration.getElementSeparator();
		char segmentTerminator = configuration.getSegmentTerminator();
		char subElementSeparator = configuration.getSubElementSeparator();

		while ((read = is.read()) != -1) {
			char ch = (char) read;

			if (ch == '\n') {
				location.nextLine();
			} else {
				location.nextChar();
			}

			if (ch == elementSeparator || ch == segmentTerminator) {
				// The caller will close the segment
				// Flush the accumulator
				location.startSubElement();
				emitSubElement(accumulator, location.getElementIdentifier());
				return ch;
			}

			if (ch == subElementSeparator) {
				location.startSubElement();
				emitSubElement(accumulator, location.getElementIdentifier());
				blank(accumulator);

				continue;
			}

			accumulator.append(ch);
		}

		throw new EOFException("Unexpected EOF when parsing sub-element: "
				+ accumulator.toString());
	}

	private void emitSubElement(StringBuffer accumulator,
			ImmutableEdiElementIdentifier elementId) throws SAXException {
		contentHandler.startElement(NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT, EMPTY_ATTRIBUTES);
		ValueTransformer<String, String> transformer = ediValueTransformers
				.get(elementId);
		String value = accumulator.toString();
		if (transformer != null) {
			value = transformer.transform(value);
		}
		char[] accumulatorChars = value.toCharArray();
		contentHandler.characters(accumulatorChars, 0, accumulatorChars.length);
		contentHandler.endElement(NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT);
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
		private EdiElementIdentifier elementId = new EdiElementIdentifier(
				"ISA", 0);

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
			elementId.setSegment(segment);
			elementId.setElement(0);
			elementId.clearSubElement();
		}

		public void startElement() {
			elementId.nextElement();
		}

		public void endElement() {
			elementId.clearSubElement();
		}

		public void startSubElement() {
			if (elementId.hasSubElement()) {
				elementId.nextSubElement();
			} else {
				elementId.setSubElement(0);
			}
		}

		public ImmutableEdiElementIdentifier getElementIdentifier() {
			return elementId;
		}

		@Override
		public String toString() {
			StringBuffer locationBuffer = new StringBuffer();
			locationBuffer.append("line ").append(line).append(", character ")
					.append(character).append(", segment ")
					.append(elementId.getSegment());
			if (elementId.getIndex() > 0) {
				locationBuffer.append(", element ")
						.append(elementId.getIndex());
			}
			if (elementId.hasSubElement()) {
				locationBuffer.append("subelement ").append(
						elementId.getSubElement());
			}
			String wellKnownName = wellKnownElements.get(elementId);
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
}
