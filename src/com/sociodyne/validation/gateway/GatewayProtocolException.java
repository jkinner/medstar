package com.sociodyne.validation.gateway;

public class GatewayProtocolException extends GatewayInteractionException {

  private static final long serialVersionUID = -102651545910240904L;

  public GatewayProtocolException(String message, Throwable cause) {
    super(message, cause);
  }

  public GatewayProtocolException(String message) {
    super(message);
  }

  public GatewayProtocolException(Throwable cause) {
    super(cause);
  }
}
