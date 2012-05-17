// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne;

/**
 * Parses a name using heuristics that are specific to a locale.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public interface PatientNameParser {

  /** Returns the {@code PatientName} represented by the {@code name}. */ 
  PatientName valueOf(String name);
}
