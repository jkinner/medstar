package com.sociodyne.parser.edi;

public class EdiException extends Exception {

  protected EdiException() {
    // Only used by subclasses
  }

  public EdiException(String message) {
    super(message);
  }

  public EdiException(String message, Throwable cause) {
    super(message, cause);
  }

  public EdiException(Throwable cause) {
    super(cause);
  }
}
