package com.sociodyne.validation.edi;

import com.sociodyne.test.Mock;
import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;
import com.sociodyne.validation.edi.ElementParser.NoSubElementsParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;
import org.xml.sax.ContentHandler;

import com.google.common.collect.Lists;

public class HLParserTest extends MockEdiParserTest {
	@Mock(Mock.Type.NICE) ParserFactory<SegmentParser> segmentParserFactory;
	@Mock(Mock.Type.NICE) ParserFactory<NoSubElementsParser> noSubElementsParserFactory;
	@Mock(Mock.Type.NICE) ParserFactory<ElementParser> elementParserFactory;
	@Mock(Mock.Type.NICE) NoSubElementsParser noSubElementsParser;
	@Mock(Mock.Type.NICE) ElementParser elementParser;

	List<SegmentParser> segmentParsers = Lists.newArrayList();

	public void setUp() throws Exception {
		super.setUp();
		expect(segmentParserFactory.create(anyObject(Reader.class), anyObject(Configuration.class),
				anyObject(ContentHandler.class), anyObject(Location.class)))
				.andStubAnswer(
					new IAnswer<SegmentParser>() {
						int i = 0;
						public SegmentParser
							answer() throws Throwable {
							return segmentParsers.get(i++);
					}});
	}

	protected SegmentParser createMockSegmentParser() {
		SegmentParser mockParser = createMock(SegmentParser.class);
		segmentParsers.add(mockParser);
		return mockParser;
	}

	public void testSingleHLSegmentNoContents_succeeds() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("1**20*1~".getBytes()));

		SegmentParser segmentParser = createMockSegmentParser();
		expect(segmentParser.parse()).andReturn(-1);
		expectStartBlock("20");
		expectEndBlock();
		replay();
		
		HLParser hlParser = new HLParser(reader, configuration, location, contentHandler,
				new EdiReader.Context(), segmentParserFactory, noSubElementsParserFactory);
		hlParser.parse();

		assertEquals(1, hlParser.info.thisLevel);
		assertNull(hlParser.info.parentLevel);
		assertEquals(20, hlParser.info.code);
		assertEquals(true, hlParser.info.hasChildNode);
	}

	public void testSingleHLSegmentWithContents_succeeds() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("1**20*1~".getBytes()));

		SegmentParser segmentParser = createMockSegmentParser();
		expect(segmentParser.parse()).andReturn((int)'~');
		expectStartBlock("20");
		expectEndBlock();

		replay();
		
		HLParser hlParser = new HLParser(reader, configuration, location, contentHandler,
				new EdiReader.Context(), segmentParserFactory, noSubElementsParserFactory);
		hlParser.parse();

		assertEquals(1, hlParser.info.thisLevel);
		assertNull(hlParser.info.parentLevel);
		assertEquals(20, hlParser.info.code);
		assertEquals(true, hlParser.info.hasChildNode);
	}
}
