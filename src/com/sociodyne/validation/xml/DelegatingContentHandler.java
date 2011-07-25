package com.sociodyne.validation.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Delegates all calls to a delegate. This class does not define how the delegate is assigned,
 * which is up to the implementer of a derived class.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public abstract class DelegatingContentHandler implements ContentHandler {

  protected ContentHandler next;

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (next != null) {
      next.characters(ch, start, length);
    }
  }

  public void endDocument() throws SAXException {
    if (next != null) {
      next.endDocument();
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (next != null) {
      next.endElement(uri, localName, qName);
    }
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    if (next != null) {
      next.endPrefixMapping(prefix);
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    if (next != null) {
      next.ignorableWhitespace(ch, start, length);
    }
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (next != null) {
      next.processingInstruction(target, data);
    }
  }

  public void setDocumentLocator(Locator locator) {
    if (next != null) {
      next.setDocumentLocator(locator);
    }
  }

  public void skippedEntity(String name) throws SAXException {
    if (next != null) {
      next.skippedEntity(name);
    }
  }

  public void startDocument() throws SAXException {
    if (next != null) {
      next.startDocument();
    }
  }

  public void startElement(String uri, String localName, String qName, Attributes atts)
      throws SAXException {
    if (next != null) {
      next.startElement(uri, localName, qName, atts);
    }
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (next != null) {
      next.startPrefixMapping(prefix, uri);
    }
  }
}
