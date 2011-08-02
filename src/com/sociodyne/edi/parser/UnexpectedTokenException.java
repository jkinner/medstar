package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

@SuppressWarnings("serial")
public class UnexpectedTokenException extends EdiException {

  private Token actual;
  private Token.Type[] expected;
  private Token exact;

  public UnexpectedTokenException(String message, Throwable cause) {
    // Primarily used for wrapping parse exceptions
    super(message, cause);
  }

  public UnexpectedTokenException(Token actual, Token.Type... expected) {
    this.actual = actual;
    this.expected = expected;
  }

  public UnexpectedTokenException(Token actual, Token exact) {
    this.actual = actual;
    this.exact = exact;
  }

  public UnexpectedTokenException(Throwable cause, Token actual, Token.Type... expected) {
    this.actual = actual;
    this.expected = expected;
    initCause(cause);
  }

  @Override
  public String getMessage() {
    if (actual == null) {
      // This exception may have been wrapped.
      return super.getMessage();
    }

    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("Unexpected token '");
    if (actual != null) {
      stringBuffer.append(actual.toString());
    } else {
      stringBuffer.append("EOF");
    }

    stringBuffer.append("'. ");
    if (exact != null) {
      stringBuffer.append("Expected: " + exact.toString());
    } else {
      stringBuffer.append("Expected one of: ");
      if (expected.length > 0) {
        for (final Token.Type expectedType : expected) {
          stringBuffer.append(expectedType.toString());
          stringBuffer.append(", ");
        }
        stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
      } else {
        stringBuffer.append("EOF");
      }
    }

    return stringBuffer.toString();
  }
}
