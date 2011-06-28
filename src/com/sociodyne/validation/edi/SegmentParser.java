package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.EdiAttributes;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

class SegmentParser extends Parser {
	private String segmentIdentifier;
	private ElementParser elementParser;
	private ContentHandler contentHandler;

	/** The XML element name for a segment. */
	private static final String SEGMENT_ELEMENT = "segment";

	/** The XML attribute for the type of segment. */
	private static final String TYPE_ATTRIBUTE = "type";

	public SegmentParser(Reader reader, Configuration configuration, Location location,
			ContentHandler contentHandler, ElementParser elementParser) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
		this.elementParser = elementParser;
	}

	private void startSegment(String segmentIdentifier, Location location)
			throws SAXException {
		location.startSegment(segmentIdentifier);
		EdiAttributes attributes = new EdiAttributes();
		attributes.put(new QName("", TYPE_ATTRIBUTE, ""), segmentIdentifier);
	
		// TODO(jkinner): Fill in attributes
		// We are placing elements in the default namespace, so the qName ==
		// localPart
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, SEGMENT_ELEMENT,
				SEGMENT_ELEMENT, attributes);
	}

	private void endSegment(String segmentIdentifier) throws SAXException {
		// The whole segment is finished. Send the "end" event.
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, SEGMENT_ELEMENT,
				SEGMENT_ELEMENT);
	}

	protected Character handleCharacter(char ch) throws IOException, SAXException {
		char elementSeparator = configuration.getElementSeparator();

		if (ch == elementSeparator) {
			// We will hit an element separator after the segment identifier;
			// anywhere else is an error
			if (segmentIdentifier == null) {
				segmentIdentifier = accumulator.toString();

				startSegment(segmentIdentifier, location);

				// Now we recurse into the segment parsing
				Integer descendResult = elementParser.parse();
				if (descendResult != null && descendResult == -1) {
					// Not expecting an EOF
					throw new EOFException("Unexpected EOF while parsing elements (missing segment terminator?)");
				}
				endSegment(segmentIdentifier);
				segmentIdentifier = null;

				blank(accumulator);

				// We only stop in EOF case
				return null;
			}
		} else if (ch == configuration.getSegmentTerminator()) {
			if (accumulator.length() > 0) {
				// Handles a segment with zero elements
				segmentIdentifier = accumulator.toString();
				startSegment(segmentIdentifier, location);
				endSegment(segmentIdentifier);
				segmentIdentifier = null;
			}

			blank(accumulator);

			return null;
		}

		accumulator.append(ch);
		return null;
	}

	protected void handleTerminalToken(Character ch) throws IOException, SAXException {
		if ((ch != null && ch != configuration.getSegmentTerminator())
			|| (ch == null && accumulator.length() > 0)) {
			throw new EOFException("Unexpected EOF when parsing segment "
					+ " (missing segment terminator '" + configuration.getSegmentTerminator()
					+ "'). Remaining tokens: '" + accumulator.toString() + "'");
		}
	}
}