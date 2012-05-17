// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

import java.io.EOFException;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SubElementListParser implements Parser {

  private final Tokenizer tokenizer;
  private final EdiLocation location;

  @Inject
  public SubElementListParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location) {
    this.tokenizer = tokenizer;
    this.location = location;
  }

  public boolean matches(Token token) {
    return token.getType() == Token.Type.SUB_ELEMENT_SEPARATOR;
  }

  public Token parse(Token token) throws EdiException, IOException {
    if (!matches(token)) {
      throw new UnexpectedTokenException(token, Token.Type.SUB_ELEMENT_SEPARATOR);
    }

    while (true) {
      token = tokenizer.nextToken();
      if (token == null) {
        throw new EdiException("Unexpected EOF in sub-element", new EOFException());
      }

      if (token.getType() == Token.Type.WORD) {
        // Good
        location.nextSubElement();
        token = tokenizer.nextToken();
        if (token == null) {
          throw new EdiException("Unexpected EOF in sub-element", new EOFException());
        }

        switch (token.getType()) {
        case SUB_ELEMENT_SEPARATOR:
          // Okay
          break;
        case ELEMENT_SEPARATOR:
          return token;
        case SEGMENT_TERMINATOR:
          return token;
        default:
          throw new UnexpectedTokenException(token, Token.Type.ELEMENT_SEPARATOR,
              Token.Type.SUB_ELEMENT_SEPARATOR, Token.Type.SEGMENT_TERMINATOR);
        }
      }
    }
  }
}
