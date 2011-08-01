package com.sociodyne.edi;

import com.google.inject.Provider;


/**
 * A sequence that increases as long as it is resident.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class TransientSequenceGenerator implements Provider<Integer> {
  int counter = 0;

  public Integer get() {
    counter++;
    return counter;
  }
}
