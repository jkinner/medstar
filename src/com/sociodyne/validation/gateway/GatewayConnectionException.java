// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.validation.gateway;

public class GatewayConnectionException extends GatewayInteractionException {

  private static final long serialVersionUID = 5241902759218973343L;

  public GatewayConnectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public GatewayConnectionException(String message) {
    super(message);
  }

  public GatewayConnectionException(Throwable cause) {
    super(cause);
  }
}
