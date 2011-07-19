package com.sociodyne.parser.edi;

import java.io.EOFException;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Parses a single EDI segment.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class SegmentParser implements Parser {

  protected final Tokenizer tokenizer;
  protected final ParserFactory<ElementListParser> elementListParserFactory;
  protected final EdiLocation location;
  protected final EdiHandler handler;

  @Inject
  SegmentParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location,
      @Assisted EdiHandler handler, ParserFactory<ElementListParser> elementListParserFactory) {
    this.tokenizer = tokenizer;
    this.elementListParserFactory = elementListParserFactory;
    this.location = location;
    this.handler = handler;
  }

  public boolean matches(Token token) {
    return token.getType() == Token.Type.WORD;
  }

  public Token parse(Token startToken) throws EdiException, IOException {
    if (!matches(startToken)) {
      throw new EdiException("Unrecognized token " + startToken);
    }

    final String segmentIdentifier = startToken.getValue();
    location.startSegment(segmentIdentifier);
    handler.startSegment(segmentIdentifier);

    final ElementListParser elementListParser = elementListParserFactory.create(location,
        tokenizer, handler);

    Token token;
    PARSE_LOOP:
    do {
      token = tokenizer.nextToken();
      if (token == Token.ELEMENT_SEPARATOR) {
        token = elementListParser.parse(token);
        switch (token.getType()) {
        case SEGMENT_TERMINATOR:
          break PARSE_LOOP;
        default:
          throw new UnexpectedTokenException(token, Token.SEGMENT_TERMINATOR);
        }
      } else if (token == null) {
        throw new EOFException();
      } else {
        throw new UnexpectedTokenException(token, Token.ELEMENT_SEPARATOR);
      }
    } while (token != null);

    location.endSegment();
    handler.endSegment();
    return token;
  }
}
