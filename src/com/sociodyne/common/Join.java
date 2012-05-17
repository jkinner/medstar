// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.common;

/**
 * Utility class to join collections together using separators.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class Join {
  private Join() {
    // Utility class
  }
  
  public static String join(String joiner, Iterable<String> parts) {
    StringBuffer joinedStringBuffer = new StringBuffer();

    if (parts.iterator().hasNext()) {
      for (String part : parts) {
        joinedStringBuffer.append(part);
        joinedStringBuffer.append(joiner);
      }

      // Remove that extra joiner we put on there.
      joinedStringBuffer.delete(joinedStringBuffer.length() - joiner.length(),
          joinedStringBuffer.length());
    }
    
    return joinedStringBuffer.toString();
  }
}
