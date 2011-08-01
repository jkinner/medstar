package com.sociodyne.edi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Marks a binding as being applicable to a particular segment name.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
public @interface SegmentNamed {
  String value();
}
