package com.sociodyne.edi.parser;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import com.sociodyne.edi.Configuration;
import com.sociodyne.edi.EdiException;
import com.sociodyne.test.Mock;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.Reader;

public class ElementListParserTest extends MockEdiParserTest {

  @Mock(Mock.Type.NICE)
  ParserFactory<SubElementListParser> subElementListParserFactory;
  @Mock
  SubElementListParser subElementListParser;

  Configuration configuration = new Configuration.Builder().setSegmentTerminator('~')
      .setElementSeparator(':').setSubElementSeparator('|').build();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    expect(
        subElementListParserFactory.create(anyObject(EdiLocation.class),
            anyObject(Tokenizer.class), anyObject(EdiHandler.class))).andStubReturn(
        subElementListParser);
  }

  public void testOneElement_succeeds() throws Exception {
    handler.startElement("123");
    handler.endElement();

    replay();

    final Reader r = new CharArrayReader(new String("123~").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, fileLocation);
    final ElementListParser parser = new ElementListParser(tokenizer, location, handler,
        subElementListParserFactory);
    assertEquals(Token.SEGMENT_TERMINATOR, parser.parse(Token.ELEMENT_SEPARATOR));
  }

  public void testOneSubElement_noSegmentTerminator_throwsEof() throws Exception {
    handler.startElement("123");

    replay();

    final Reader r = new CharArrayReader(new String("123").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, fileLocation);
    final ElementListParser parser = new ElementListParser(tokenizer, location, handler,
        subElementListParserFactory);
    try {
      parser.parse(Token.ELEMENT_SEPARATOR);
      fail("Expected ParseException caused by EOFException");
    } catch (final EOFException e) {
      // Expected
    }
  }

  public void testTwoElements_succeeds() throws Exception {
    handler.startElement("123");
    handler.endElement();
    handler.startElement("234");
    handler.endElement();

    replay();

    final Reader r = new CharArrayReader(new String("123:234~").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, fileLocation);
    final ElementListParser parser = new ElementListParser(tokenizer, location, handler,
        subElementListParserFactory);
    parser.parse(Token.ELEMENT_SEPARATOR);
  }

  public void testTwoSubElements_withElementSeparator_succeeds() throws Exception {
    expect(subElementListParser.parse(eq(new Token(Token.Type.SUB_ELEMENT_SEPARATOR)))).andReturn(
        Token.ELEMENT_SEPARATOR);

    handler.startElement("123");
    handler.endElement();
    handler.startElement("123");
    handler.endElement();

    replay();

    final Reader r = new CharArrayReader(new String("123|"/* 234: */+ "123~").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, fileLocation);
    final ElementListParser parser = new ElementListParser(tokenizer, location, handler,
        subElementListParserFactory);
    parser.parse(Token.ELEMENT_SEPARATOR);
  }

  public void testTwoSubElements_noSegmentTerminator_throwsEof() throws Exception {
    expect(subElementListParser.parse(eq(new Token(Token.Type.SUB_ELEMENT_SEPARATOR)))).andThrow(
        new EdiException(new EOFException()));

    handler.startElement("123");

    replay();

    final Reader r = new CharArrayReader(new String("123|234").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, fileLocation);
    final ElementListParser parser = new ElementListParser(tokenizer, location, handler,
        subElementListParserFactory);
    try {
      parser.parse(Token.ELEMENT_SEPARATOR);
      fail("Expected ParseException caused by EOFException");
    } catch (final EdiException e) {
      assertTrue("Expected EOFException",
          EOFException.class.isAssignableFrom(e.getCause().getClass()));
    }
  }

}
