// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import com.sociodyne.test.Mock;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

public class HlLoopParserTest extends MockEdiParserTest {

  @Mock
  ParserFactory<ElementListParser> elementListParserFactory;
  @Mock
  ElementListParser elementListParser;
  @Mock
  SegmentParserFactory segmentParserFactory;
  @Mock
  SegmentParser segmentParser;

  public void testSingleHlLoop_succeeds() throws Exception {
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

    readTokens(Token.ELEMENT_SEPARATOR,
    // Read by the elementListParser mock
        Token.word("DTP")
    // Read by the child segment parser
    );

    handler.startLoop("HL");
    handler.startSegment("HL");
    // DTP is parsed by a mock; no handler events generated
    handler.endSegment();
    handler.endLoop();

    replay();

    final HlLoopParser hlParser = new HlLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    hlParser.parse(Token.word("HL"));
  }

  public void testSingleHlLoop_readsSingleTrnSegment_succeeds() throws Exception {
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andReturn(elementListParser);
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("TRN"))).andReturn(segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "TRN")))).andReturn(
        Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("DTP"))).andReturn(segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP")))).andReturn(
        Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR,
    // Read by the elementListParser mock
        Token.word("TRN"),
        // Read by the child segment parser
        Token.word("DTP")
    // Read by the child segment parser
    );

    handler.startLoop("HL");
    handler.startSegment("HL");
    // TRN is parsed by a mock; no handler events generated
    handler.endSegment();
    handler.endLoop();
    // DTP is parsed by a mock; no handler events generated

    replay();

    final HlLoopParser hlParser = new HlLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    hlParser.parse(Token.word("HL"));
  }

  public void testSingleHlLoop_readsMultipleTrnSegment_succeeds() throws Exception {
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andReturn(elementListParser);
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("TRN"))).andReturn(segmentParser).times(2);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "TRN")))).andReturn(
        Token.SEGMENT_TERMINATOR).times(2);
    expect(
        segmentParserFactory.create(anyObject(Tokenizer.class), eq(location), eq(handler),
            eq("DTP"))).andReturn(segmentParser);
    expect(segmentParser.parse(eq(new Token(Token.Type.WORD, "DTP")))).andReturn(
        Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR,
    // Read by the elementListParser mock
        Token.word("TRN"),
        // Read by the child segment parser
        Token.word("TRN"),
        // Read by the child segment parser
        Token.word("DTP")
    // Read by the child segment parser
    );

    handler.startLoop("HL");
    handler.startSegment("HL");
    // TRN is parsed by a mock; no handler events generated
    // DTP is parsed by a mock; no handler events generated
    handler.endSegment();
    handler.endLoop();

    replay();

    final HlLoopParser hlParser = new HlLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    hlParser.parse(Token.word("HL"));
  }

  public void testSingleHlLoop_terminatesAfterOneSegment() throws Exception {
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

    readTokens(Token.ELEMENT_SEPARATOR,
    // Read by the elementListParser mock
        Token.word("DTP"),
        // Read by the child segment parser
        Token.word("EB"));

    handler.startLoop("HL");
    handler.startSegment("HL");
    // DTP is parsed by a mock; no handler events generated
    // EB is not parsed
    handler.endSegment();
    handler.endLoop();

    replay();

    final HlLoopParser hlParser = new HlLoopParser(tokenizer, location, handler,
        elementListParserFactory, segmentParserFactory);
    hlParser.parse(Token.word("HL"));

    assertNotNull(tokenizer.nextToken());
  }
}
