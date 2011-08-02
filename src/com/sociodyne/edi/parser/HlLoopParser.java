package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

import java.io.EOFException;
import java.io.IOException;

import com.google.inject.assistedinject.Assisted;

public class HlLoopParser extends SegmentParser {

  private static final Token HL_TOKEN = Token.word("HL");
  private final SegmentParserFactory segmentParserFactory;

  public HlLoopParser(Tokenizer tokenizer, @Assisted EdiLocation location,
      @Assisted EdiHandler handler, ParserFactory<ElementListParser> elementListParserFactory,
      SegmentParserFactory segmentParserFactory) {
    super(tokenizer, location, handler, elementListParserFactory);
    this.segmentParserFactory = segmentParserFactory;
  }

  @Override
  public boolean matches(Token token) {
    return token != null && token.equals(HL_TOKEN);
  }

  @Override
  public Token parse(Token startToken) throws EdiException, IOException {
    if (!matches(startToken)) {
      throw new UnexpectedTokenException(startToken, HL_TOKEN);
    }

    final String loopSegment = startToken.getValue();
    location.startSegment(loopSegment);
    handler.startLoop(loopSegment);
    handler.startSegment(loopSegment);

    // We're in an HL loop.
    Token token = tokenizer.nextToken();
    final ElementListParser elementListParser = elementListParserFactory.create(location,
        tokenizer, handler);

    // HL requires elements, so let the element list parser complain if the
// parse fails.
    token = elementListParser.parse(token);
    if (token.getType() != Token.Type.SEGMENT_TERMINATOR) {
      throw new UnexpectedTokenException(token, Token.Type.SEGMENT_TERMINATOR);
    }

    token = tokenizer.nextToken();
    // Now we read exactly one segment
    if (token == null) {
      throw new EdiException(new EOFException());
    }

    while (token.getType() == Token.Type.WORD && token.getValue().equals("TRN")) {
      // TRNs are informational segments; allow as many as you want.
      final SegmentParser segmentParser = segmentParserFactory.create(tokenizer, location, handler,
          token.getValue());
      // This will be a segment terminator
      token = segmentParser.parse(token);
      // The rest of the algorithm expects a word, which should be next.
      token = tokenizer.nextToken();
      if (token == null) {
        throw new EdiException(new EOFException());
      }
    }

    if (token.getType() != Token.Type.WORD) {
      throw new UnexpectedTokenException(token, Token.Type.WORD);
    }

    final SegmentParser segmentParser = segmentParserFactory.create(tokenizer, location, handler,
        token.getValue());
    token = segmentParser.parse(token);

    handler.endSegment();
    handler.endLoop();

    return token;
  }
}
