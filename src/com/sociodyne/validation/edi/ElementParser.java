package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

class ElementParser extends Parser {
	protected ContentHandler contentHandler;
	private SubElementParser subElementParser;

	/** The ISA sub-element identifier location. */
	private static final ImmutableEdiLocation ISA_SUBELEMENT_IDENTIFIER =
		ImmutableEdiLocation.of("ISA", 16);

	/** The XML element name for an element. */
	private static final String ELEMENT_ELEMENT = "element";

	public static final Map<ImmutableEdiLocation, ValueTransformer<String, String>>
		EMPTY_TRANSFORMERS = ImmutableMap.<ImmutableEdiLocation, ValueTransformer<String, String>>of();

	@Inject
	ElementParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler,
			ParserFactory<SubElementParser> subElementParserFactory) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
		this.subElementParser = subElementParserFactory.create(reader, configuration,
				contentHandler, location);
	}

	/**
	 * Private ctor, used ONLY by sub-classes that use a differe sub-element parser type
	 * (most notably {@link NoSubElementsParser}).
	 * <p>
	 * Any class that uses this ctor must call setSubElementParserFactory() in its ctor.
	 */
	protected ElementParser(Reader reader, Configuration configuration,
			Location location, ContentHandler contentHandler) {
		super(reader, configuration, location);
		this.contentHandler = contentHandler;
	}

	protected void setSubElementParserFactory(
			ParserFactory<? extends SubElementParser> subElementParserFactory) {
		this.subElementParser = subElementParserFactory.create(reader, configuration,
				contentHandler, location);
	}

	protected Character handleCharacter(char ch) throws IOException, SAXException {
		if (ch == configuration.getElementSeparator()
				|| ch == configuration.getSegmentTerminator()) {
			startElement(accumulator, location);
			endElement();

			if (location.getEdiLocation().equals(ISA_SUBELEMENT_IDENTIFIER)) {
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

	protected void startElement(
			StringBuffer accumulator, Location location)
			throws IOException, SAXException {
		location.startElement();
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, ELEMENT_ELEMENT,
				ELEMENT_ELEMENT, EdiReader.EMPTY_ATTRIBUTES);

		String value = accumulator.toString();
		char[] accumulatorChars = value.toCharArray();
		if (accumulatorChars.length > 0) {
			contentHandler.characters(accumulatorChars, 0, accumulatorChars.length);
		}
	}

	protected void endElement() throws SAXException {
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, ELEMENT_ELEMENT, ELEMENT_ELEMENT);
	}

	static class NoSubElementsParser extends SubElementParser {

		@Inject
		NoSubElementsParser(@Assisted Reader reader, @Assisted Configuration configuration,
				@Assisted Location location, @Assisted ContentHandler contentHandler) {
			super(reader, configuration, location, contentHandler);
		}

		@Override
		protected Character handleCharacter(char ch) throws IOException,
				SAXException {
			throw new SAXException("Unexpected sub-element");
		}

		@Override
		protected void handleTerminalToken(Character ch) throws IOException,
				SAXException {
		}
	}
}