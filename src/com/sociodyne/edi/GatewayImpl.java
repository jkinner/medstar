package com.sociodyne.edi;

import java.lang.annotation.Annotation;

/**
 * Used to identify which {@code Gateways} value a particular Guice binding belongs to.
 * Usage: {@code Gateways.forGateway(Gateways.<enum_value>)}
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 * @see <a href="http://code.google.com/p/google-guice/wiki/BindingAnnotations#@Named">Guice @Named
        annotation</a>
 */
// Suppressed to avoid annotation impl warning
@SuppressWarnings({"all"})
public class GatewayImpl implements Gateway {
  private final Gateways value;

  public GatewayImpl(Gateways value) {
    this.value = value;
  }

  public Class<? extends Annotation> annotationType() {
    return Gateway.class;
  }

  public Gateways value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof GatewayImpl) {
      GatewayImpl that = (GatewayImpl)o;
      return value == that.value;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return (127 * "value".hashCode()) ^ value.hashCode();
  }

  @Override
  public String toString() {
    return "@" + Gateway.class.getName() + "{value=" + value + "}";
  }
}
