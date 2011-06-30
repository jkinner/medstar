package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LoopEndParser extends ElementParser {
	private static final String LOOP_ELEMENT = "loop";

	LoopInfo info = new LoopInfo();

	@Inject
	LoopEndParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler,
			EdiReader.Context context,
			ParserFactory<NoSubElementsParser> noSubElementsParserFactory) {
		super(reader, configuration, location, contentHandler, context);
		this.setSubElementParserFactory(noSubElementsParserFactory);
	}

	@Override
	protected void startElement(StringBuffer accumulator, Location location)
			throws IOException, SAXException {
		location.startElement();
		switch (location.getEdiLocation().getIndex()) {
			case 1:
				// TODO(jkinner): Verify the loop code from LS to LE
				info.code = Integer.parseInt(accumulator.toString());
				endLoop();
				break;
			default:
				throw new SAXException("Unknown HL element: "
						+ location.getEdiLocation().getIndex());
		}
	}

	protected void endLoop() throws SAXException {
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, LOOP_ELEMENT, LOOP_ELEMENT);
	}

	@Override
	protected void endElement() throws SAXException {
		location.endElement();
	}

	static class LoopInfo {
		/* LS01 */
		int code;
	}
}
