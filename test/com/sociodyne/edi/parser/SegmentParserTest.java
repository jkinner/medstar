// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import com.sociodyne.edi.EdiException;
import com.sociodyne.test.Mock;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

import java.io.EOFException;

public class SegmentParserTest extends MockEdiParserTest {

  @Mock(Mock.Type.NICE)
  ParserFactory<ElementListParser> elementListParserFactory;
  @Mock
  ElementListParser elementListParser;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    expect(
        elementListParserFactory.create(anyObject(EdiLocation.class), anyObject(Tokenizer.class),
            anyObject(EdiHandler.class))).andStubReturn(elementListParser);
  }

  public void testOneSegment_succeeds() throws Exception {
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);
    readTokens(Token.ELEMENT_SEPARATOR
    // This will trigger an element list parse, which is all that will happen.
    );

    handler.startSegment(eq("ISA"));
    handler.endSegment();

    replay();

    final SegmentParser parser = new SegmentParser(tokenizer, location, handler,
        elementListParserFactory);
    assertEquals(Token.SEGMENT_TERMINATOR, parser.parse(new Token(Token.Type.WORD, "ISA")));
  }

  public void testOneSegment_elementListParserThrowsEof_throwsEof() throws Exception {
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR))).andThrow(
        new EdiException(new EOFException()));
    readTokens(Token.ELEMENT_SEPARATOR);

    handler.startSegment(eq("ISA"));

    replay();

    final SegmentParser parser = new SegmentParser(tokenizer, location, handler,
        elementListParserFactory);
    try {
      parser.parse(new Token(Token.Type.WORD, "ISA"));
      fail("Expected ParseException caused by EOFException");
    } catch (final EdiException e) {
      assertTrue("Expected EOFException",
          EOFException.class.isAssignableFrom(e.getCause().getClass()));
    }
  }

  public void testTwoSegments_stopsAfterOneSegment() throws Exception {
    expect(elementListParser.parse(eq(Token.ELEMENT_SEPARATOR)))
        .andReturn(Token.SEGMENT_TERMINATOR);

    readTokens(Token.ELEMENT_SEPARATOR,
    // This token will not be reached
        Token.ELEMENT_SEPARATOR);

    handler.startSegment(eq("ISA"));
    handler.endSegment();

    replay();

    final SegmentParser parser = new SegmentParser(tokenizer, location, handler,
        elementListParserFactory);
    parser.parse(new Token(Token.Type.WORD, "ISA"));
    // The other token should not have been read.
    tokenizer.nextToken();
  }

}
