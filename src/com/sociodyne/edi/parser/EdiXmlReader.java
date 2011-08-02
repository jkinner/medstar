package com.sociodyne.edi.parser;

import com.sociodyne.common.Exceptions;
import com.sociodyne.edi.Configuration;
import com.sociodyne.edi.EdiException;
import com.sociodyne.parser.Location;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class EdiXmlReader implements XMLReader {

  /** Feature URI for wrapping exceptions with location information. */
  public static final String WRAP_EXCEPTIONS_FEATURE = "http://www.sociodyne.com/xmlns/xml/wrapExceptions";

  /** Property URI for setting the default segment terminator. */
  public static final String SEGMENT_TERMINATOR_PROPERTY = "http://www.sociodyne.com/xmlns/edi/segmentTerminator";

  /** Property URI for setting the default element separator. */
  public static final String ELEMENT_SEPARATOR_PROPERTY = "http://www.sociodyne.com/xmlns/edi/elementSeparator";

  /** Property URI for setting the default subelement separator. */
  public static final String SUBELEMENT_SEPARATOR_PROPERTY = "http://www.sociodyne.com/xmlns/edi/subelementSeparator";

  /** SAX namespace feature URI. */
  private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

  /** SAX namespace prefix feature URI. */
  private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";

  // TODO(jkinner): Implement validation
  /** SAX validation feature URI. */
  private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";

  /** Configured segment terminator. */
  private char segmentTerminator = '~';

  /** Configured element separator. */
  private char elementSeparator = '*';

  /** Configured sub-element separator. */
  private char subelementSeparator = '|';

  /** Value of the {@link #WRAP_EXCEPTIONS_FEATURE} feature. */
  private boolean defaultWrapExceptions = true;

  /** Value of the {@link #NAMESPACE_FEATURE} feature. */
  private boolean defaultUseNamespaces = true;

  // TODO(jkinner): Implement using namespace prefixes (if applicable).
  /** Value of the {@link #NAMESPACE_PREFIXES_FEATURE} feature. */
  private final boolean defaultUseNamespacePrefixes = false;

  private final ParserFactory<SegmentListParser> segmentListParserFactory;

  private final ParserFactory<SegmentParser> segmentParserFactory;

  // XMLReader fields
  private ContentHandler contentHandler;
  private DTDHandler dtdHandler;
  private EntityResolver entityResolver;
  private ErrorHandler errorHandler;

  @Inject
  EdiXmlReader(ParserFactory<SegmentListParser> segmentListParserFactory,
      ParserFactory<SegmentParser> segmentParserFactory) {
    this.segmentListParserFactory = segmentListParserFactory;
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
      return defaultUseNamespaces;
    }
    if (feature.equals(NAMESPACE_PREFIXES_FEATURE)) {
      return false;
    }
    if (feature.equals(WRAP_EXCEPTIONS_FEATURE)) {
      return defaultWrapExceptions;
    }
    if (feature.startsWith("http://xml.org/sax/")) {
      throw new SAXNotSupportedException(feature);
    } else {
      throw new SAXNotRecognizedException(feature);
    }
  }

  public Object getProperty(String property) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    // TODO(jkinner): Decide what properties to expose
    throw new SAXNotSupportedException();
  }

  public void parse(InputSource inputSource) throws IOException, SAXException {
    final Reader characterStream = inputSource.getCharacterStream();
    if (characterStream != null) {
      parseReader(characterStream);
    } else if (inputSource.getByteStream() != null) {
      parseReader(new InputStreamReader(inputSource.getByteStream()));
    }
  }

  public void parse(String url) throws IOException, SAXException {
    final InputStream is = new URL(url).openStream();
    final Reader r = new InputStreamReader(is, "UTF-8");
    parseReader(r);
  }

  protected void parseReader(Reader reader) throws IOException, SAXException {
    final Configuration configuration = new Configuration.Builder()
        .setSegmentTerminator(segmentTerminator).setElementSeparator(elementSeparator).build();

    final Location fileLocation = new Location();
    final EdiLocation location = new EdiLocation(fileLocation, "ISA", 0);
    
    final Tokenizer tokenizer = new Tokenizer(reader, configuration, fileLocation);

    contentHandler.startDocument();
    contentHandler.startElement(EdiXmlAdapter.NAMESPACE_URI, EdiXmlAdapter.EDI_ELEMENT,
        EdiXmlAdapter.EDI_ELEMENT, EdiXmlAdapter.Attributes.of());

    final EdiHandler handler = new EdiXmlAdapter(contentHandler);
// handler = (EdiHandler) Proxy.newProxyInstance(getClass().getClassLoader(),
// new Class<?>[] { EdiHandler.class },
// new LoggingInvocationHandler(handler, System.err));
    final EdiHandler isaFilterHandler = new IsaEdiHandler(configuration, handler);
    final SegmentParser isaSegmentParser = segmentParserFactory.create(location, tokenizer,
        isaFilterHandler);
    try {
      Token isaToken = tokenizer.nextToken();
      if (isaToken == null) {
        // No content. Produce a valid, empty document.
        contentHandler.endElement(EdiXmlAdapter.NAMESPACE_URI, EdiXmlAdapter.EDI_ELEMENT,
            EdiXmlAdapter.EDI_ELEMENT);
        contentHandler.endDocument();
        return;
      }
      if (!isaToken.equals(Token.word("ISA"))) {
        throw new UnexpectedTokenException(isaToken, Token.word("ISA"));
      }

      // Parse the ISA segment with a special EdiHandler to pull the sub-element
      isaToken = isaSegmentParser.parse(isaToken);

      if (!(isaToken == Token.SEGMENT_TERMINATOR)) {
        throw new UnexpectedTokenException(isaToken, Token.SEGMENT_TERMINATOR);
      }
    } catch (EdiException e) {
      if (defaultWrapExceptions) {
        e = Exceptions.wrap(e, location.toString());
      }
      throw new SAXException(e);
    }

    // separator out of the segment.
    final SegmentListParser segmentListParser = segmentListParserFactory.create(location,
        tokenizer, handler);
    final Token token = tokenizer.nextToken();
    if (token != null) {
      try {
        segmentListParser.parse(token);
      } catch (EdiException e) {
        if (e.getCause() instanceof IOException) {
          // Throw IOExceptions directly.
          IOException ioe = (IOException) e.getCause();
          if (defaultWrapExceptions) {
            ioe = Exceptions.wrap(ioe, location.toString());
          }

          throw ioe;
        }

        if (defaultWrapExceptions) {
          e = Exceptions.wrap(e, location.toString());
        }
        throw new SAXException(e);
      }
    }

    contentHandler.endElement(EdiXmlAdapter.NAMESPACE_URI, EdiXmlAdapter.EDI_ELEMENT,
        EdiXmlAdapter.EDI_ELEMENT);
    contentHandler.endDocument();
    // TODO(jkinner): Warn if no other headers?
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

  public void setFeature(String feature, boolean enabled) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    if (feature.equals(NAMESPACES_FEATURE)) {
      defaultUseNamespaces = enabled;
    }
    if (feature.equals(NAMESPACE_PREFIXES_FEATURE)) {
      throw new SAXNotSupportedException(feature);
    }
    if (feature.equals(WRAP_EXCEPTIONS_FEATURE)) {
      defaultWrapExceptions = enabled;
    }

    if (feature.startsWith("http://xml.org/sax/")) {
      throw new SAXNotSupportedException(feature);
    } else {
      throw new SAXNotRecognizedException(feature);
    }
  }

  public void setProperty(String name, Object value) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    if (name.equals(SEGMENT_TERMINATOR_PROPERTY)) {
      segmentTerminator = singleCharValue(value);
    }

    if (name.equals(ELEMENT_SEPARATOR_PROPERTY)) {
      elementSeparator = singleCharValue(value);
    }

    if (name.equals(ELEMENT_SEPARATOR_PROPERTY)) {
      subelementSeparator = singleCharValue(value);
    }

    throw new SAXNotSupportedException();
  }

  private char singleCharValue(Object value) {
    Preconditions.checkArgument(Character.class.isAssignableFrom(value.getClass())
        || String.class.isAssignableFrom(value.getClass()));
    if (Character.class.isAssignableFrom(value.getClass())) {
      return (Character) value;
    } else if (String.class.isAssignableFrom(value.getClass())) {
      final String stringValue = (String) value;
      Preconditions.checkArgument(stringValue.length() == 1, value);
      return stringValue.charAt(0);
    }

    // NOT_REACHED
    throw new IllegalArgumentException("Expected a String or char");
  }

  public static class Factory {

    private static Injector ediXmlReaderInjector = Guice.createInjector(new ParserModule());

    public static EdiXmlReader create() {
      return ediXmlReaderInjector.getInstance(EdiXmlReader.class);
    }
  }

}
