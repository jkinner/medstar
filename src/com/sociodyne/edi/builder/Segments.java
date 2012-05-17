// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

/**
 * Factory for {@link SegmentNamed} annotation instances.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class Segments {
  public static SegmentNamed named(String segmentName) {
    return new SegmentNamedImpl(segmentName);
  }
}
