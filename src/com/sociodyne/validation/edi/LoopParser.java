package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.EdiAttributes;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.IOException;
import java.io.Reader;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LoopParser extends ElementParser {
	private static final String LOOP_ELEMENT = "loop";
	private static final String TYPE_ATTRIBUTE = "type";

	private ParserFactory<SegmentParser> segmentParserFactory;

	LoopInfo info = new LoopInfo();

	@Inject
	LoopParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler,
			EdiReader.Context context,
			ParserFactory<SegmentParser> segmentParserFactory,
			ParserFactory<NoSubElementsParser> noSubElementsParserFactory) {
		super(reader, configuration, location, contentHandler, context);
		this.segmentParserFactory = segmentParserFactory;
		this.setSubElementParserFactory(noSubElementsParserFactory);
	}

	@Override
	protected void startElement(StringBuffer accumulator, Location location)
			throws IOException, SAXException {
		location.startElement();
		switch (location.getEdiLocation().getIndex()) {
			case 1:
				info.code = Integer.parseInt(accumulator.toString());
				startLoop(info.code, location);
				SegmentParser parser = segmentParserFactory.create(reader, configuration,
						contentHandler, location);
				// Loops parse recursively until a LE (LoopEnd) is encountered. That parser
				// will end the loop.
				parser.parse();
				break;
			default:
				throw new SAXException("Unknown HL element: "
						+ location.getEdiLocation().getIndex());
		}
	}

	protected void startLoop(int code, Location location)
			throws SAXException {
		location.startElement();
		EdiAttributes attributes = new EdiAttributes();
		attributes.put(new QName("", TYPE_ATTRIBUTE, ""), Integer.toString(code));
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, LOOP_ELEMENT,
				LOOP_ELEMENT, attributes);
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
