package com.sociodyne;

import com.google.common.base.Preconditions;


/**
 * Helpful methods for working with {@code String}s.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class Strings {
  private Strings() {
    // Utility class
  }

  public static final int NO_MAXIMUM_SIZE = -1;

  public static String padLeft(String string, char padChar, int minimumSize) {
    return padLeft(string, padChar, minimumSize, new StringBuffer(), NO_MAXIMUM_SIZE);
  }

  public static String padLeft(String string, char padChar, int minimumSize,
      StringBuffer paddedStringBuffer) {
    return padLeft(string, padChar, minimumSize, paddedStringBuffer, NO_MAXIMUM_SIZE);
  }

  public static String padLeft(String string, char padChar, int minimumSize,
      StringBuffer paddedStringBuffer, int maximumSize) {
    Preconditions.checkArgument(maximumSize == -1 || string.length() <= maximumSize,
        "String must be less than " + maximumSize + " characters.");

    for (int i = string.length(); i < minimumSize; i++) {
      paddedStringBuffer.append(padChar);
    }
    paddedStringBuffer.append(string);

    return paddedStringBuffer.toString();
  }
}
