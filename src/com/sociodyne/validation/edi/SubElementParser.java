package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

class SubElementParser extends Parser {
	ContentHandler contentHandler;
	Map<ImmutableEdiLocation, ValueTransformer<String, String>> ediValueTransformers;

	/** The XML element name for a sub-element. */
	private static final String SUBELEMENT_ELEMENT = "subelement";
	

	protected SubElementParser(Reader reader, Configuration configuration, Location location,
			ContentHandler contentHandler,
			Map<ImmutableEdiLocation, ValueTransformer<String, String>> ediValueTransformers) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
		this.ediValueTransformers = ediValueTransformers;
	}

	@Override
	protected Character handleCharacter(char ch) throws IOException,
			SAXException {
		if (ch == configuration.getElementSeparator()
				|| ch == configuration.getSegmentTerminator()) {
			// The caller will close the segment
			// Flush the accumulator
			location.startSubElement();
			emitSubElement(accumulator, location.getEdiLocation());
			return ch;
		}

		if (ch == configuration.getSubElementSeparator()) {
			location.startSubElement();
			emitSubElement(accumulator, location.getEdiLocation());
			blank(accumulator);

			return null;
		}

		accumulator.append(ch);
		return null;
	}

	@Override
	protected void handleTerminalToken(Character ch) throws IOException, SAXException {
		if (ch == null
			|| (ch != configuration.getElementSeparator()
				&& ch != configuration.getSegmentTerminator())) {
			throw new EOFException("Sub-element not terminated by element separator "
					+ "or segment terminator");
		}
		
	}

	private void emitSubElement(StringBuffer accumulator,
			ImmutableEdiLocation elementId) throws SAXException {
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT, EdiReader.EMPTY_ATTRIBUTES);
		ValueTransformer<String, String> transformer = ediValueTransformers
				.get(elementId);
		String value = accumulator.toString();
		if (transformer != null) {
			value = transformer.transform(value);
		}
		char[] accumulatorChars = value.toCharArray();
		contentHandler.characters(accumulatorChars, 0, accumulatorChars.length);
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT);
	}


}