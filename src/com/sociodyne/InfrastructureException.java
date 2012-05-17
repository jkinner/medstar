// Copyright 2011, Sociodyne LLC. All rights reserved.
// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne;

public class InfrastructureException extends RuntimeException {

  private static final long serialVersionUID = 6991461754232404466L;

  public InfrastructureException() {
  }

  public InfrastructureException(String message) {
    super(message);
  }

  public InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }

  public InfrastructureException(Throwable cause) {
    super(cause);
  }
}
