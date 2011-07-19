package com.sociodyne.parser.edi;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XmlEdiWriter implements ContentHandler {
	private Type type;
	private final EdiWriter writer;
	private boolean startedLoopSegment = false;
	private Stack<String> loopSegments = new Stack<String>();
	private boolean hasSegmentContents = false;
	private boolean isEmptyElement = true;
	private boolean isEmptySubElement = true;

	private enum Type {
		SEGMENT,
		ELEMENT,
		SUBELEMENT,
		UNKNOWN
	}

	public XmlEdiWriter(EdiWriter writer) {
		this.writer = writer;
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		try {
			switch (type) {
			case SEGMENT:
				break;
			case ELEMENT:
				hasSegmentContents = true;
				isEmptyElement = false;
				writer.startElement(new String(arg0, arg1, arg2));
				break;
			case SUBELEMENT:
				isEmptySubElement = false;
				writer.subElement(new String(arg0, arg1, arg2));
				break;
			}
		} catch (EdiException e) {
			throw new SAXException(e);
		}
	}

	public void endDocument() throws SAXException {
		if (type != null) {
			try {
				if (hasSegmentContents) {
					writer.endSegment();
				}
			} catch (EdiException e) {
				throw new SAXException(e);
			}
		}
	}

	public void endElement(String namespaceUri, String localName, String qName)
			throws SAXException {
		try {
			if (localName.equals(EdiXmlAdapter.SEGMENT_ELEMENT) && hasSegmentContents) {
				if (hasSegmentContents) {
					writer.endSegment();
					hasSegmentContents = false;
				}
			} else if (localName.equals(EdiXmlAdapter.LOOP_ELEMENT)) {
				loopSegments.pop();
			} else if (localName.equals(EdiXmlAdapter.ELEMENT_ELEMENT)) {
				if (isEmptyElement) {
					writer.startElement("");
				}
				writer.endElement();
			} else if (localName.equals(EdiXmlAdapter.SUBELEMENT_ELEMENT)) {
				if (isEmptySubElement) {
					writer.subElement("");
				}
			}
		} catch (EdiException e) {
			throw new SAXException(e);
		}
	}

	public void endPrefixMapping(String arg0) throws SAXException {

	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
	}

	public void setDocumentLocator(Locator arg0) {

	}

	public void skippedEntity(String arg0) throws SAXException {

	}

	public void startDocument() throws SAXException {

	}

	public void startElement(String namespaceUri, String localName, String qName,
			Attributes attributes) throws SAXException {
		Type newType;

		if (namespaceUri.equals(EdiXmlAdapter.NAMESPACE_URI)) {
			if (localName.equals(EdiXmlAdapter.SEGMENT_ELEMENT)) {
				String segmentIdentifier = attributes.getValue(EdiXmlAdapter.TYPE_ATTRIBUTE);
				try {
					if (startedLoopSegment) {
						// Loops are serialized in a special way. The starting segment has all the
						// following segments as children. So, the first loop segment needs special
						// handling.
						if (hasSegmentContents) {
							writer.endSegment();
							hasSegmentContents = false;
							startedLoopSegment = false;
						}
					}
					newType = Type.SEGMENT;
					writer.startSegment(segmentIdentifier);
				} catch (EdiException e) {
					throw new SAXException(e);
				}

				if (!loopSegments.empty() && segmentIdentifier.equals(loopSegments.peek())) {
					startedLoopSegment = true;
				}
			} else if (localName.equals(EdiXmlAdapter.ELEMENT_ELEMENT)) {
				isEmptyElement = true;
				newType = Type.ELEMENT;
			} else if (localName.equals(EdiXmlAdapter.SUBELEMENT_ELEMENT)) {
				isEmptySubElement = true;
				newType = Type.SUBELEMENT;
			} else {
				newType = Type.UNKNOWN;
			}

			if (localName.equals(EdiXmlAdapter.LOOP_ELEMENT)) {
				loopSegments.push(attributes.getValue(EdiXmlAdapter.TYPE_ATTRIBUTE));
			}
		} else {
			newType = Type.UNKNOWN;
		}

		type = newType;
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
	}

}
