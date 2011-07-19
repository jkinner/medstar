package com.sociodyne.parser.edi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.sociodyne.test.Mock;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

import com.google.common.collect.ImmutableSet;

public class LoopParserTest extends MockEdiParserTest {
	@Mock ParserFactory<ElementListParser> elementListParserFactory;
	@Mock ElementListParser elementListParser;
	@Mock SegmentParserFactory segmentParserFactory;
	@Mock SegmentParser segmentParser;

	public void testRepeatedLoopSegements_succeeds() throws Exception {
		expect(elementListParserFactory.create(anyObject(EdiLocation.class),
				anyObject(Tokenizer.class), anyObject(EdiHandler.class)))
			.andReturn(elementListParser);
		expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
			.andReturn(Token.SEGMENT_TERMINATOR)
			.times(2);
		readTokens(
				Token.ELEMENT_SEPARATOR,
				Token.word("EB"),
				Token.ELEMENT_SEPARATOR,
				null
		);

		handler.startLoop("EB");
		handler.startSegment("EB");
		expectLastCall().times(2);
		handler.endSegment();
		expectLastCall().times(2);
		handler.endLoop();

		replay();

		LoopParser loopParser = new LoopParser(tokenizer, location, handler,
				elementListParserFactory, segmentParserFactory, ImmutableSet.<String>of());
		loopParser.parse(new Token(Token.Type.WORD, "EB"));
	}

	public void testRepeatedLoopSegements_separatedByNonLoopSegment_succeeds() throws Exception {
		expect(elementListParserFactory.create(anyObject(EdiLocation.class),
				anyObject(Tokenizer.class), anyObject(EdiHandler.class)))
			.andReturn(elementListParser);
		expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
			.andReturn(Token.SEGMENT_TERMINATOR)
			.times(2);
		expect(segmentParserFactory.create(anyObject(Tokenizer.class),
			eq(location), eq(handler), eq("DTP")))
			.andReturn(segmentParser);
		expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP"))))
			.andReturn(Token.SEGMENT_TERMINATOR);
		readTokens(
				Token.ELEMENT_SEPARATOR,
				Token.word("DTP"),
				// Element separator would have been read by the segment parser
				Token.word("EB"),
				Token.ELEMENT_SEPARATOR
		);

		handler.startLoop("EB");
		handler.startSegment("EB");
		expectLastCall().times(2);
		handler.endSegment();
		expectLastCall().times(2);
		// No start segment generated by DTP parser, which is a mock
		handler.endLoop();

		replay();

		LoopParser loopParser = new LoopParser(tokenizer, location, handler,
				elementListParserFactory, segmentParserFactory, ImmutableSet.<String>of("DTP"));
		loopParser.parse(new Token(Token.Type.WORD, "EB"));
	}

	public void testReadTerminalSegment_stopsParsing() throws Exception {
		expect(elementListParserFactory.create(anyObject(EdiLocation.class),
				anyObject(Tokenizer.class), anyObject(EdiHandler.class)))
			.andReturn(elementListParser);
		expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
			.andReturn(Token.SEGMENT_TERMINATOR);
		expect(segmentParserFactory.create(anyObject(Tokenizer.class),
			eq(location), eq(handler), eq("DTP")))
			.andReturn(segmentParser);
		expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP"))))
			.andReturn(Token.SEGMENT_TERMINATOR);
		readTokens(
				Token.ELEMENT_SEPARATOR,
				Token.word("DTP"),
				// Element separator would have been read by the segment parser
				Token.word("NM1"),
				Token.ELEMENT_SEPARATOR
		);

		handler.startLoop("EB");
		handler.startSegment("EB");
		handler.endSegment();
		// No start segment generated by DTP parser, which is a mock
		handler.endLoop();
		// No start segment generated by NM1 parser, which is a mock

		replay();

		LoopParser loopParser = new LoopParser(tokenizer, location, handler,
				elementListParserFactory, segmentParserFactory, ImmutableSet.<String>of("DTP"));
		assertEquals(Token.word("NM1"), loopParser.parse(new Token(Token.Type.WORD, "EB")));
		// Make sure the element separator wasn't read
		assertNotNull(tokenizer.nextToken());
	}

}