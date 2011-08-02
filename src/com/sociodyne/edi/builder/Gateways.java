package com.sociodyne.edi.builder;


/**
 * Gateways that are supported by the system.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public enum Gateways {
  CMS;
  
  public static Gateway forGateway(Gateways gateway) {
    return new GatewayImpl(gateway);
  }
}
