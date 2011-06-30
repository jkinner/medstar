package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.EdiAttributes;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SegmentParser extends Parser {
	private String segmentIdentifier;
	protected ContentHandler contentHandler;
	private ElementParser defaultElementParser;
	private Map<String, ParserFactory<? extends ElementParser>> elementParserFactories;

	/** The XML element name for a segment. */
	private static final String SEGMENT_ELEMENT = "segment";

	/** The XML attribute for the type of segment. */
	private static final String TYPE_ATTRIBUTE = "type";

	private Map<String, ElementParser> elementParsers = Maps.newHashMap();

	@Inject
	SegmentParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler, 
			Map<String, ParserFactory<? extends ElementParser>> elementParserFactories,
			ParserFactory<ElementParser> defaultElementParserFactory) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
		this.defaultElementParser = defaultElementParserFactory.create(reader, configuration,
				contentHandler, location);
		this.elementParserFactories = elementParserFactories;
	}

	protected void startSegment(String segmentIdentifier, Location location)
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

	protected void endSegment(String segmentIdentifier) throws SAXException {
		// The whole segment is finished. Send the "end" event.
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, SEGMENT_ELEMENT,
				SEGMENT_ELEMENT);
	}

	/**
	 * Creates an element parser and caches it for this run. The parsers can then be stateful.
	 * 
	 * @param segmentIdentifier the name of the segment being parsed
	 * @return an element parser for that segment.
	 */
	protected ElementParser createCustomElementParser(String segmentIdentifier) {
		if (! elementParsers.containsKey(segmentIdentifier)) {
			ParserFactory<? extends ElementParser> descenderFactory =
				elementParserFactories.get(segmentIdentifier);
			ElementParser elementParser = null;
			if (descenderFactory != null) {
				elementParser = descenderFactory.create(reader, configuration, contentHandler,
						location);
				elementParsers.put(segmentIdentifier, elementParser);
			}
			return elementParser;
		} else {
			return elementParsers.get(segmentIdentifier);
		}
	}

	protected Character handleCharacter(char ch) throws IOException, SAXException {
		char elementSeparator = configuration.getElementSeparator();

		if (ch == elementSeparator) {
			// We will hit an element separator after the segment identifier;
			// anywhere else is an error
			if (segmentIdentifier == null) {
				segmentIdentifier = accumulator.toString();

				// Now we recurse into the segment parsing
				Integer descendResult;
				Parser descender = defaultElementParser;
				ElementParser customElementParser = createCustomElementParser(segmentIdentifier);
				if (customElementParser != null) {
					// The child parser is responsible for rendering the entire segment
					location.startSegment(segmentIdentifier);
					descender = customElementParser;
				} else {
					startSegment(segmentIdentifier, location);
				}

				descendResult = descender.parse();
				if (descendResult != null && descendResult == -1) {
					// Not expecting an EOF
					throw new EOFException("Unexpected EOF while parsing elements "
							+" (missing segment terminator?)");
				}
				
				if (customElementParser == null) {
					// If a descenderFactory was found, it was responsible for rendering the segment
					endSegment(segmentIdentifier);
				}
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