package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;
import com.sociodyne.test.parser.edi.MockEdiParserTest;

import java.io.EOFException;

public class SubElementListParserTest extends MockEdiParserTest {

  public void testOneSubElement_succeeds() throws Exception {
    readTokens(Token.word("123"), Token.SEGMENT_TERMINATOR);

    replay();

    final SubElementListParser parser = new SubElementListParser(tokenizer, location);
    parser.parse(Token.SUB_ELEMENT_SEPARATOR);
  }

  public void testOneSubElement_noSegmentTerminator_throwsEof() throws Exception {
    readTokens(Token.word("123"));

    replay();

    final SubElementListParser parser = new SubElementListParser(tokenizer, location);
    try {
      parser.parse(Token.SUB_ELEMENT_SEPARATOR);
      fail("Expected ParseException caused by EOFException");
    } catch (final EdiException e) {
      assertTrue("Expected EOFException",
          EOFException.class.isAssignableFrom(e.getCause().getClass()));
    }
  }

  public void testTwoSubElements_succeeds() throws Exception {
    readTokens(Token.word("123"), Token.SUB_ELEMENT_SEPARATOR, Token.word("234"),
        Token.SEGMENT_TERMINATOR);

    replay();

    final SubElementListParser parser = new SubElementListParser(tokenizer, location);
    parser.parse(Token.SUB_ELEMENT_SEPARATOR);
  }

  public void testTwoSubElements_withElementSeparator_succeeds() throws Exception {
    readTokens(Token.word("123"), Token.SUB_ELEMENT_SEPARATOR, Token.word("234"),
        Token.ELEMENT_SEPARATOR);

    replay();

    final SubElementListParser parser = new SubElementListParser(tokenizer, location);
    parser.parse(Token.SUB_ELEMENT_SEPARATOR);
  }

  public void testTwoSubElements_noSegmentTerminator_throwsEof() throws Exception {
    readTokens(Token.word("123"), Token.SUB_ELEMENT_SEPARATOR, Token.word("234"));

    replay();

    final SubElementListParser parser = new SubElementListParser(tokenizer, location);
    try {
      parser.parse(Token.SUB_ELEMENT_SEPARATOR);
      fail("Expected ParseException caused by EOFException");
    } catch (final EdiException e) {
      assertTrue("Expected EOFException",
          EOFException.class.isAssignableFrom(e.getCause().getClass()));
    }
  }

}
