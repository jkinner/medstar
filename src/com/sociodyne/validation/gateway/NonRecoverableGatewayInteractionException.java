// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.validation.gateway;

public class NonRecoverableGatewayInteractionException extends GatewayInteractionException {

  private static final long serialVersionUID = -1782930465023405982L;

  public NonRecoverableGatewayInteractionException(String message, Throwable cause) {
    super(message, cause);
  }

  public NonRecoverableGatewayInteractionException(String message) {
    super(message);
  }

  public NonRecoverableGatewayInteractionException(Throwable cause) {
    super(cause);
  }
}
