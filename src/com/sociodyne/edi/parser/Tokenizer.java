// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.Configuration;
import com.sociodyne.parser.Location;

import java.io.IOException;
import java.io.Reader;

public class Tokenizer {

  private final Reader r;
  private final Configuration config;
  private Token lookAhead;
  private final Location location;

  public Tokenizer(Reader r, Configuration config, Location location) {
    this.r = r;
    this.config = config;
    this.location = location;
  }

  public Token nextToken() throws IOException {
    if (lookAhead != null) {
      final Token result = lookAhead;
      lookAhead = null;
      return result;
    }

    int read;
    final StringBuffer accumulator = new StringBuffer();

    read = r.read();
    if (read != -1) {
      char ch = (char) read;
      location.nextChar();
      if (ch == '\n') {
        location.nextLine();
      }

      if (config.isSegmentTerminator(ch)) {
        return Token.SEGMENT_TERMINATOR;
      } else if (config.isElementSeparator(ch)) {
        return Token.ELEMENT_SEPARATOR;
      }
      if (config.isSubElementSeparator(ch)) {
        return Token.SUB_ELEMENT_SEPARATOR;
      }

      do {
        accumulator.append(ch);
        read = r.read();
        if (read != -1) {
          ch = (char) read;
          location.nextChar();
          if (ch == '\n') {
            location.nextLine();
          }
        }
      } while (read != -1 && !isWordTerminal(ch));

      if (read != -1) {
        // We have read a terminal off the stream. Save the token for the next
// call.
        if (config.isSegmentTerminator(ch)) {
          lookAhead = Token.SEGMENT_TERMINATOR;
        }
        if (config.isElementSeparator(ch)) {
          lookAhead = Token.ELEMENT_SEPARATOR;
        }
        if (config.isSubElementSeparator(ch)) {
          lookAhead = Token.SUB_ELEMENT_SEPARATOR;
        }
      }
      return new Token(Token.Type.WORD, accumulator.toString());
    }

    return null;
  }

  private boolean isWordTerminal(char ch) {
    return config.isSegmentTerminator(ch) || config.isElementSeparator(ch)
        || config.isSubElementSeparator(ch);
  }
}
