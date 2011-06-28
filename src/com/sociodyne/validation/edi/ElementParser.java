package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

class ElementParser extends Parser {
	private ContentHandler contentHandler;
	private SubElementParser subElementParser;
	private Map<ImmutableEdiLocation, ValueTransformer<String, String>> ediValueTransformers;

	/** The ISA sub-element identifier location. */
	private static final ImmutableEdiLocation ISA_SUBELEMENT_IDENTIFIER =
		ImmutableEdiLocation.of("ISA", 16);

	/** The XML element name for an element. */
	private static final String ELEMENT_ELEMENT = "element";

	public ElementParser(Reader reader, Configuration configuration, Location location,
			ContentHandler contentHandler, SubElementParser subElementParser,
			Map<ImmutableEdiLocation, ValueTransformer<String, String>> ediValueTransformers) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
		this.subElementParser = subElementParser;
		this.ediValueTransformers = ediValueTransformers;
	}

	protected Character handleCharacter(char ch) throws IOException, SAXException {
		if (ch == configuration.getElementSeparator()
				|| ch == configuration.getSegmentTerminator()) {
			startElement(accumulator, location);
			endElement();

			if (location.getEdiLocation().equals(
					ISA_SUBELEMENT_IDENTIFIER)) {
				configuration.setSubElementSeparator(accumulator.charAt(0));
			}

			blank(accumulator);

			if (ch == configuration.getSegmentTerminator()) {
				// Terminate this parser; the caller will close the segment
				return ch;
			}

			return null;
		}

		// TODO(jkinner): Make this a flag after ISA is parsed instead of
		// string comparison
		if (ch == configuration.getSubElementSeparator()) {
			if (! location.getEdiLocation().getSegment().equals("ISA")) {
				// Parse subElements until the end of the element
				location.startElement();
				startElement(accumulator, location);
				Integer terminatorToken = subElementParser.parse();
				endElement();
				location.endElement();
	
				blank(accumulator);

				if (terminatorToken == configuration.getSegmentTerminator()) {
					return configuration.getSegmentTerminator();
				}
	
				return null;
			} else {
				throw new SAXException("Sub-element separator not permitted in ISA segment");
			}
		}

		accumulator.append(ch);
		return null;
	}

	protected void handleTerminalToken(Character ch) throws IOException, SAXException {
		if (ch == null) {
			throw new EOFException("Unexpected EOF when parsing element: "
					+ accumulator.toString() + " (missing segment terminator '"
					+ configuration.getSegmentTerminator() + "' or element separator '"
					+ configuration.getElementSeparator() + "')");
		} else if(ch != configuration.getSegmentTerminator()) {
			// This should never, ever happen
			throw new SAXException("Unexpected token '" + ch + "' after parsing element.");
		}

	}

	private void startElement(
			StringBuffer accumulator, Location location)
			throws SAXException {
		location.startElement();
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, ELEMENT_ELEMENT,
				ELEMENT_ELEMENT, EdiReader.EMPTY_ATTRIBUTES);

		ValueTransformer<String, String> transformer = ediValueTransformers
				.get(location.getEdiLocation());
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

	private void endElement() throws SAXException {
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, ELEMENT_ELEMENT, ELEMENT_ELEMENT);
	}
}