package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

class SubElementParser extends Parser {
	ContentHandler contentHandler;

	/** The XML element name for a sub-element. */
	private static final String SUBELEMENT_ELEMENT = "subelement";
	

	@Inject
	SubElementParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
	}

	@Override
	protected Character handleCharacter(char ch) throws IOException,
			SAXException {
		if (ch == configuration.getElementSeparator()
				|| ch == configuration.getSegmentTerminator()) {
			// The caller will close the segment
			// Flush the accumulator
			location.startSubElement();
			startStopSubElement(accumulator, location.getEdiLocation());
			return ch;
		}

		if (ch == configuration.getSubElementSeparator()) {
			location.startSubElement();
			startStopSubElement(accumulator, location.getEdiLocation());
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

	private void startStopSubElement(StringBuffer accumulator,
			ImmutableEdiLocation elementId) throws IOException, SAXException {
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT, EdiReader.EMPTY_ATTRIBUTES);
		String value = accumulator.toString();
		char[] accumulatorChars = value.toCharArray();
		contentHandler.characters(accumulatorChars, 0, accumulatorChars.length);
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, SUBELEMENT_ELEMENT,
				SUBELEMENT_ELEMENT);
	}


}