package com.sociodyne.validation.gateway;

public class GatewayInteractionException extends Exception {

  private static final long serialVersionUID = -5617234749281375491L;

  public GatewayInteractionException(String message, Throwable cause) {
    super(message, cause);
  }

  public GatewayInteractionException(String message) {
    super(message);
  }

  public GatewayInteractionException(Throwable cause) {
    super(cause);
  }
}
