// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import com.google.common.base.Objects;


/**
 * Used to identify which segment a particular Guice binding belongs to.
 * Usage: {@code Segments.named(<segment_name>)}
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 * @see <a href="http://code.google.com/p/google-guice/wiki/BindingAnnotations#@Named">Guice @Named
        annotation</a>
 *
 */
//Suppressed to avoid annotation impl warning
@SuppressWarnings({"all"})
public class SegmentNamedImpl implements SegmentNamed, Serializable {
  private String segmentName;

  SegmentNamedImpl(String segmentName) {
    this.segmentName = segmentName;
  }

  public Class<? extends Annotation> annotationType() {
    return SegmentNamed.class;
  }

  public String value() {
    return segmentName;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SegmentNamedImpl) {
      SegmentNamedImpl that = (SegmentNamedImpl)o;
      return Objects.equal(segmentName, that.segmentName);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return (127 * "value".hashCode()) ^ segmentName.hashCode();
  }

  @Override
  public String toString() {
    return "@" + SegmentNamed.class.getName() + "{value=" + segmentName + "}";
  }
}
