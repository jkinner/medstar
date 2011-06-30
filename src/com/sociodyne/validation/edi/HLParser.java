package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.EdiAttributes;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.IOException;
import java.io.Reader;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class HLParser extends ElementParser {
	private static final String BLOCK_ELEMENT = "block";
	private static final String CODE_ATTRIBUTE = "code";

	private ParserFactory<SegmentParser> segmentParserFactory;

	HLInfo info = new HLInfo();

	@Inject
	HLParser(@Assisted Reader reader, @Assisted Configuration configuration,
			@Assisted Location location, @Assisted ContentHandler contentHandler,
			ParserFactory<SegmentParser> segmentParserFactory,
			ParserFactory<NoSubElementsParser> noSubElementsParserFactory) {
		super(reader, configuration, location, contentHandler);
		this.segmentParserFactory = segmentParserFactory;
		this.setSubElementParserFactory(noSubElementsParserFactory);
	}

	@Override
	protected void startElement(StringBuffer accumulator, Location location)
			throws IOException, SAXException {
		location.startElement();
		switch (location.getEdiLocation().getIndex()) {
			case 1:
				info.thisLevel = Integer.parseInt(accumulator.toString());
				break;
			case 2:
				if (accumulator.length() > 0) {
					info.parentLevel = Integer.parseInt(accumulator.toString());
				}
				break;
			case 3:
				info.code = Integer.parseInt(accumulator.toString());
				break;
			case 4:
				info.hasChildNode = Integer.parseInt(accumulator.toString()) != 0;
				// Descend, parsing new segments
				startBlock(info.code, location);
				SegmentParser parser = segmentParserFactory.create(reader, configuration,
						contentHandler, location);
				parser.parse();
				endBlock();
				break;
			default:
				throw new SAXException("Unknown HL element: "
						+ location.getEdiLocation().getIndex());
		}
	}

	protected void startBlock(int code, Location location)
			throws SAXException {
		location.startElement();
		EdiAttributes attributes = new EdiAttributes();
		attributes.put(new QName("", CODE_ATTRIBUTE, ""), Integer.toString(code));
		contentHandler.startElement(EdiConstants.NAMESPACE_URI, BLOCK_ELEMENT,
				BLOCK_ELEMENT, attributes);
	}

	protected void endBlock() throws SAXException {
		contentHandler.endElement(EdiConstants.NAMESPACE_URI, BLOCK_ELEMENT, BLOCK_ELEMENT);
	}

	@Override
	protected void endElement() throws SAXException {
		location.endElement();
	}

	static class HLInfo {
		/* HL01 */
		int thisLevel;
		/* HL02 */
		@Nullable Integer parentLevel;
		/* HL03 */
		int code;
		/* HL04 */
		boolean hasChildNode;
	}
}
