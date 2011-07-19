package com.sociodyne.parser.edi;

import com.sociodyne.parser.Location;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import java.io.CharArrayReader;
import java.io.Reader;

public class TokenizerTest extends MockTest {

  @Mock(Mock.Type.NICE)
  Location location;

  Configuration configuration = new Configuration.Builder().setSegmentTerminator('~')
      .setElementSeparator(':').setSubElementSeparator('|').build();

  public void testReadSegmentTerminator() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new char[] { '~' });
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(Token.SEGMENT_TERMINATOR, tokenizer.nextToken());
  }

  public void testReadElementSeparator() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new char[] { ':' });
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(Token.ELEMENT_SEPARATOR, tokenizer.nextToken());
  }

  public void testReadSubElementSeparator() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new char[] { '|' });
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(Token.SUB_ELEMENT_SEPARATOR, tokenizer.nextToken());
  }

  public void testReadWord_eofOkay() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new String("abc").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(new Token(Token.Type.WORD, "abc"), tokenizer.nextToken());
  }

  public void testReadWord_eofOkay_nextTokenEof() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new String("abc").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(new Token(Token.Type.WORD, "abc"), tokenizer.nextToken());
    assertNull("Expected null token on EOF", tokenizer.nextToken());
  }

  public void testReadWordThenElementSeparator_succeeds() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new String("abc:").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(new Token(Token.Type.WORD, "abc"), tokenizer.nextToken());
    assertEquals(Token.ELEMENT_SEPARATOR, tokenizer.nextToken());
  }

  public void testReadSimpleSegment_succeeds() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new String("abc:def~").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(new Token(Token.Type.WORD, "abc"), tokenizer.nextToken());
    assertEquals(Token.ELEMENT_SEPARATOR, tokenizer.nextToken());
    assertEquals(new Token(Token.Type.WORD, "def"), tokenizer.nextToken());
    assertEquals(Token.SEGMENT_TERMINATOR, tokenizer.nextToken());
    assertNull("Expected null token on EOF", tokenizer.nextToken());
  }

  public void testReadSegment_withSubElements_succeeds() throws Exception {
    replay();

    final Reader r = new CharArrayReader(new String("abc:def|ghi~").toCharArray());
    final Tokenizer tokenizer = new Tokenizer(r, configuration, location);
    assertEquals(new Token(Token.Type.WORD, "abc"), tokenizer.nextToken());
    assertEquals(Token.ELEMENT_SEPARATOR, tokenizer.nextToken());
    assertEquals(new Token(Token.Type.WORD, "def"), tokenizer.nextToken());
    assertEquals(Token.SUB_ELEMENT_SEPARATOR, tokenizer.nextToken());
    assertEquals(new Token(Token.Type.WORD, "ghi"), tokenizer.nextToken());
    assertEquals(Token.SEGMENT_TERMINATOR, tokenizer.nextToken());
    assertNull("Expected null token on EOF", tokenizer.nextToken());
  }

}
