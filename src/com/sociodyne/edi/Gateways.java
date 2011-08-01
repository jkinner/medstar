package com.sociodyne.edi;



public enum Gateways {
  CMS;
  
  public static Gateway forGateway(Gateways gateway) {
    return new GatewayImpl(gateway);
  }
}
