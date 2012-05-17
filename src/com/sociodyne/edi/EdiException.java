// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi;

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
