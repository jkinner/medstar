package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Parses a list of segments.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class SegmentListParser implements Parser {

  private final SegmentParserFactory segmentParserFactory;
  private final Tokenizer tokenizer;
  private final EdiLocation location;
  private final EdiHandler handler;

  @Inject
  SegmentListParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location,
      @Assisted EdiHandler handler, SegmentParserFactory segmentParserFactory) {
    this.segmentParserFactory = segmentParserFactory;
    this.tokenizer = tokenizer;
    this.location = location;
    this.handler = handler;
  }

  public boolean matches(Token token) {
    return token.getType() == Token.Type.WORD;
  }

  public Token parse(Token startToken) throws EdiException, IOException {
    if (!matches(startToken)) {
      throw new UnexpectedTokenException(startToken, Token.Type.WORD);
    }

    Token token = startToken;
    do {
      final SegmentParser segmentParser = segmentParserFactory.create(tokenizer, location, handler,
          token.getValue());

      if (token.getType().equals(Token.Type.WORD)) {
        token = segmentParser.parse(token);
        if (token.getType() == Token.Type.WORD) {
          // Continue parsing the segment; it was a loop terminator
          continue;
        }

        if (token != Token.SEGMENT_TERMINATOR) {
          throw new UnexpectedTokenException(token, Token.SEGMENT_TERMINATOR);
        }
      } else {
        throw new UnexpectedTokenException(token, Token.Type.WORD);
      }

      token = tokenizer.nextToken();
    } while (token != null);

    return token;
  }

}
