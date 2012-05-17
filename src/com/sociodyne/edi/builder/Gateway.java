// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Defines for which {@link Gateway} a particular binding is for.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
public @interface Gateway {
  Gateways value();
}
