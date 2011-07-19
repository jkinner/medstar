package com.sociodyne.parser.edi;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class EdiXmlAdapter implements EdiHandler {

  /** Namespace for the {@code xml:} namespace prefix. */
  static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

  /** Namespace of elements generated by the reader. */
  static final String NAMESPACE_URI = "http://www.sociodyne.com/xmlns/edi";

  public static final String EDI_ELEMENT = "edi";

  public static final String LOOP_ELEMENT = "loop";

  public static final String ITERATION_ELEMENT = "iteration";

  public static final String TYPE_ATTRIBUTE = "type";

  public static final String ITEM_ELEMENT = "item";

  public static final String BLOCK_ELEMENT = "block";

  public static final String CODE_ATTRIBUTE = "code";

  /** The XML element name for a segment. */
  public static final String SEGMENT_ELEMENT = "segment";

  /** The XML element name for an element. */
  public static final String ELEMENT_ELEMENT = "element";

  /** The XML element name for a sub-element. */
  public static final String SUBELEMENT_ELEMENT = "subelement";

  private final ContentHandler handler;

  @Inject
  EdiXmlAdapter(@Assisted ContentHandler handler) {
    this.handler = handler;
  }

  public void startSegment(String segmentIdentifier) throws EdiException {
    start(SEGMENT_ELEMENT, Attributes.of(attributeQName(TYPE_ATTRIBUTE), segmentIdentifier));
  }

  public void endSegment() throws EdiException {
    end(SEGMENT_ELEMENT);
  }

  public void startElement(String contents) throws EdiException {
    start(ELEMENT_ELEMENT, contents);
  }

  public void endElement() throws EdiException {
    end(ELEMENT_ELEMENT);
  }

  public void subElement(String contents) throws EdiException {
    start(SUBELEMENT_ELEMENT, contents);
  }

  public void startLoop(String segmentIdentifier) throws EdiException {
    try {
      handler.startElement(NAMESPACE_URI, LOOP_ELEMENT, qName(LOOP_ELEMENT),
          Attributes.of(attributeQName(TYPE_ATTRIBUTE), segmentIdentifier));
    } catch (final SAXException e) {
      throw new EdiException(e);
    }

  }

  public void endLoop() throws EdiException {
    end(LOOP_ELEMENT);
  }

  // XML generation Methods

  /** Start an XML element with no content. */
  private void start(String element, String contents) throws EdiException {
    start(element, contents, Attributes.of());
  }

  private void start(String element, Attributes attributes) throws EdiException {
    start(element, null, attributes);
  }

  /** Start an XML element with text {@code content}. */
  private void start(String element, String contents, Attributes attributes) throws EdiException {
    try {
      handler.startElement(NAMESPACE_URI, element, qName(element), attributes);
      if (contents != null) {
        handler.characters(contents.toCharArray(), 0, contents.length());
      }
    } catch (final SAXException e) {
      throw new EdiException(e);
    }
  }

  /** End an XML element. */
  private void end(String element) throws EdiException {
    try {
      handler.endElement(NAMESPACE_URI, element, qName(element));
    } catch (final SAXException e) {
      throw new EdiException(e);
    }
  }

  private static QName attributeQName(String attributeName) {
    // We use the default namespace, so no prefix is applied.

    // We use bare attributes, so there is no namespace applied. This simplifies
// XPath
    // processing.
    return new QName("", attributeName, "");
  }

  private static String qName(String elementName) {
    // We use the default namespace, so no prefix is applied.
    return elementName;
  }

  @SuppressWarnings("serial")
  static class Attributes extends LinkedHashMap<QName, String> implements org.xml.sax.Attributes {

    private static final String CDATA_TYPE = "CDATA";
    private static final Attributes EMPTY_ATTRIBUTES = new Attributes();

    public static Attributes of() {
      return EMPTY_ATTRIBUTES;
    }

    public static Attributes of(QName attribute, String value) {
      final Attributes attributes = new Attributes();
      attributes.put(attribute, value);
      return attributes;
    }

    public int getIndex(String qName) {
      int index = 0;
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
        if (qName.equals(renderPrefix(attributeQName) + attributeQName.getLocalPart())) {
          return index;
        }
        index++;
      }

      return -1;
    }

    public int getIndex(String uri, String localName) {
      int index = 0;
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
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
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
        if (count == index) {
          return get(attributeQName);
        }
        count++;
      }

      return null;
    }

    public String getQName(int index) {
      int count = 0;
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
        if (count == index) {
          return renderPrefix(attributeQName) + attributeQName.getLocalPart();
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
      final Map.Entry<QName, String> entry = getEntryForQName(qName);
      return entry == null ? null : entry.getValue();
    }

    public String getValue(String uri, String localName) {
      final Map.Entry<QName, String> entry = getEntryForQNameParts(uri, localName);
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
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        if (count == index) {
          return attributeEntry;
        }
        count++;
      }

      throw new ArrayIndexOutOfBoundsException(index);

    }

    private Map.Entry<QName, String> getEntryForQName(String qName) {
      int index = 0;
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
        if (qName.equals(renderPrefix(attributeQName) + attributeQName.getLocalPart())) {
          return attributeEntry;
        }
        index++;
      }

      return null;
    }

    private Map.Entry<QName, String> getEntryForQNameParts(String uri, String localName) {
      for (final Map.Entry<QName, String> attributeEntry : entrySet()) {
        final QName attributeQName = attributeEntry.getKey();
        if (uri.equals(attributeQName.getNamespaceURI())
            && localName.equals(attributeQName.getLocalPart())) {
          return attributeEntry;
        }
      }

      return null;
    }

    // NOTE: These implementations are horribly inefficient; they are used
    // for testing.
    @Override
    public boolean equals(Object o) {
      // TODO(jkinner): Should order count?
      if (this == o) {
        return true;
      }
      if (o instanceof Attributes) {
        final Attributes that = (Attributes) o;
        if (size() == that.size()) {
          // Given that the sizes are equal, check each entry in this
          // map with the other
          for (final Map.Entry<QName, String> entry : entrySet()) {
            final String value = entry.getValue();
            if (!Objects.equal(value, that.get(entry.getKey()))) {
              return false;
            }
          }

          // Every entry has been verified
          return true;
        }
      }

      return false;
    }

    @Override
    public int hashCode() {
      final Object[] hashCodeParams = new Object[size() * 2];
      int i = 0;
      for (final Map.Entry<QName, String> entry : entrySet()) {
        hashCodeParams[i] = entry.getKey();
        hashCodeParams[i + 1] = entry.getValue();
        i += 2;
      }
      return Objects.hashCode(hashCodeParams);
    }
  }
}
