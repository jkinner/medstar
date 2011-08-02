package com.sociodyne.edi.parser;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import com.sociodyne.test.Mock;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

public class LsLoopParserTest extends MockEdiParserTest {

  @Mock
  ParserFactory<ElementListParser> elementListParserFactory;
  @Mock
  ElementListParser elementListParser;
  @Mock
  SegmentParserFactory segmentParserFactory;
  @Mock
  SegmentParser segmentParser;

  public void testSingleElementLoop_succeeds() throws Exception {
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andReturn(elementListParser);
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("DTP"))).andReturn(segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP")))).andReturn(
        Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory
            .create(anyObject(Tokenizer.class), eq(location), eq(handler), eq("LE"))).andReturn(
        segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "LE")))).andReturn(
        Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR, Token.word("DTP"),
    // Element separator would have been read by the segment parser
        Token.word("LE")
    // Element separator would have been read by the segment parser
    );

    handler.startLoop("LS");
    handler.startSegment("LS");
    handler.endSegment();
    handler.endLoop();

    replay();

    final LsLoopParser loopParser = new LsLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    loopParser.parse(new Token(Token.Type.WORD, "LS"));
  }

  public void testMultipleElementLoop_succeeds() throws Exception {
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andReturn(elementListParser);
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("DTP"))).andReturn(segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP")))).andReturn(
        Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory
            .create(anyObject(Tokenizer.class), eq(location), eq(handler), eq("EQ"))).andReturn(
        segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "EQ")))).andReturn(
        Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory
            .create(anyObject(Tokenizer.class), eq(location), eq(handler), eq("LE"))).andReturn(
        segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "LE")))).andReturn(
        Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR, Token.word("DTP"),
    // Element separator would have been read by the segment parser
        Token.word("EQ"),
        // Element separator would have been read by the segment parser
        Token.word("LE")
    // Element separator would have been read by the segment parser
    );

    handler.startLoop("LS");
    handler.startSegment("LS");
    handler.endSegment();
    handler.endLoop();

    replay();

    final LsLoopParser loopParser = new LsLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    loopParser.parse(new Token(Token.Type.WORD, "LS"));
  }

  public void testSingleElementLoop_withInnerLoop_succeeds() throws Exception {
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andReturn(elementListParser);
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory
            .create(anyObject(Tokenizer.class), eq(location), eq(handler), eq("EB"))).andReturn(
        segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "EB")))).andReturn(Token.word("LE"));
    expect(
        segmentParserFactory
            .create(anyObject(Tokenizer.class), eq(location), eq(handler), eq("LE"))).andReturn(
        segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "LE")))).andReturn(
        Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR, Token.word("EB"),
    // Element separator would have been read by the segment parser
        Token.word("LE")
    // Element separator would have been read by the segment parser
    );

    handler.startLoop("LS");
    handler.startSegment("LS");
    handler.endSegment();
    handler.endLoop();

    replay();

    final LsLoopParser loopParser = new LsLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    loopParser.parse(new Token(Token.Type.WORD, "LS"));
  }

}
