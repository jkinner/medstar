package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

import java.io.EOFException;
import java.io.IOException;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LoopParser extends SegmentParser {

  private final SegmentParserFactory segmentParserFactory;
  private final Set<String> nonTerminalSegments;

  @Inject
  LoopParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location,
      @Assisted EdiHandler handler, ParserFactory<ElementListParser> elementListParserFactory,
      SegmentParserFactory segmentParserFactory, Set<String> nonTerminalSegments) {
    super(tokenizer, location, handler, elementListParserFactory);
    this.segmentParserFactory = segmentParserFactory;
    this.nonTerminalSegments = nonTerminalSegments;
  }

  @Override
  public boolean matches(Token token) {
    return token.getType() == Token.Type.WORD;
  }

  @Override
  public Token parse(Token startToken) throws EdiException, IOException {
    if (!matches(startToken)) {
      throw new EdiException("Unrecognized token " + startToken);
    }

    final String loopSegment = startToken.getValue();
    handler.startLoop(loopSegment);
    location.startSegment(loopSegment);
    handler.startSegment(loopSegment);
    final ElementListParser elementListParser = elementListParserFactory.create(location,
        tokenizer, handler);
    Token token;
    boolean isNonTerminal = true;
    PARSE_LOOP:
    do {
      token = tokenizer.nextToken();
      if (token == Token.ELEMENT_SEPARATOR) {
        token = elementListParser.parse(token);
        switch (token.getType()) {
        case SEGMENT_TERMINATOR:
          // Start loop
          token = tokenizer.nextToken();
          if (token == null) {
            // EOF. It's okay because we just read a segment terminator.
            break PARSE_LOOP;
          }

          if (token.getType() == Token.Type.WORD) {
            token = parseChildSegments(loopSegment, token);
            if (token == null) {
              throw new EdiException(new EOFException());
            }
            if (token.getType() == Token.Type.WORD) {
              if (token.getValue().equals(loopSegment)) {
                // Another instance of the loop; close the last segment and open
                // a new one.
                handler.endSegment();
                handler.startSegment(loopSegment);
              } else {
                // We have a terminal segment
                isNonTerminal = false;
                break PARSE_LOOP;
              }
            }
          } else {
            throw new UnexpectedTokenException(token, Token.Type.WORD);
          }
          break;
        default:
          throw new UnexpectedTokenException(token, Token.Type.SEGMENT_TERMINATOR);
        }
      } else {
        throw new UnexpectedTokenException(token, Token.Type.ELEMENT_SEPARATOR);
      }
    } while (token != null && isNonTerminal);

    handler.endSegment();
    handler.endLoop();
    return token;
  }

  private Token parseChildSegments(String loopIdentifier, Token token) throws EdiException,
      IOException, UnexpectedTokenException {
    while (token != null) {
      // It's a segment identifier. Make sure it's part of the segment grammar.
      if (token.getType() == Token.Type.WORD
          && (token.getValue().equals(loopIdentifier) || !nonTerminalSegments.contains(token
              .getValue()))) {
        // This is either another instance of the loop we're parsing, or
        // Close the loop, start another instance, then continue.
        break;
      }

      // Parse the child segment.
      // TODO(jkinner): This needs to produce a segment parser for the
// identified
      // segment
      final SegmentParser segmentParser = segmentParserFactory.create(tokenizer, location, handler,
          token.getValue());
      token = segmentParser.parse(token);
      if (token.getType() == Token.Type.SEGMENT_TERMINATOR) {
        // It's part of the loop, so continue parsing the loop. Read
        // the next segment identifier.
        token = tokenizer.nextToken();
        if (token == null) {
          // EOF is fine since we finished parsing the segment.
          return token;
        } else if (token.getType() != Token.Type.WORD) {
          throw new UnexpectedTokenException(token, Token.Type.WORD);
        }
      } else if (token.getType() == Token.Type.WORD) {
        return token;
      } else {
        throw new UnexpectedTokenException(token, Token.Type.SEGMENT_TERMINATOR);
      }
    }

    return token;
  }

}
