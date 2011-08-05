package com.sociodyne;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * Utility class for handling sets of regular expressions.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class Patterns {
  private Patterns() {
    // Utility class
  }

  /**
   * Compares {@code input} against each {@code Pattern} in {@code patterns}, returning
   * {@code true} if any of them match the input.
   */
  public static boolean matchesAny(String input, Iterable<Pattern> patterns) {
    return findMatcher(input, patterns) != null;
  }

  /**
   * Compares {@code input} against each {@code Pattern} in {@code patterns}, returning
   * the first that matches. If no patterns match, returns {@code null}.
   */
  @Nullable public static Matcher findMatcher(String input, Iterable<Pattern> patterns) {
    Preconditions.checkNotNull(input, "input");
    Preconditions.checkNotNull(patterns, "patterns");

    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(input);
      if (matcher.matches()) {
        return matcher;
      }
    }

    return null;
  }
}
