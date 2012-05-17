// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


/**
 * Tests for {@link Patterns}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class PatternsTest extends TestCase {
  private static final Pattern ANY_PATTERN = Pattern.compile(".*");

  public void testEmptyPatternList_doesNotHaveMatcher() throws Exception {
    assertNull(Patterns.findMatcher("123", ImmutableSet.<Pattern>of()));
  }

  public void testEmptyPatternList_doesNotMatch() throws Exception {
    assertTrue(! Patterns.matchesAny("123", ImmutableSet.<Pattern>of()));
  }

  public void testSimplePattern_hasMatcher() throws Exception {
    assertNotNull(Patterns.findMatcher("123", ImmutableSet.of(ANY_PATTERN)));
  }

  public void testSimplePattern_matches() throws Exception {
    assertTrue(Patterns.matchesAny("123", ImmutableSet.of(ANY_PATTERN)));
  }

  public void testMultiplePatterns_returnsMatchingPattern() throws Exception {
    // Iteration order matters to this test; need to make sure the 999 isn't blindly returned
    assertEquals(ANY_PATTERN, Patterns.findMatcher("123", ImmutableList.of(Pattern.compile("999"),
        ANY_PATTERN)).pattern());
  }

  public void testCapturingPattern_hasMatcherInValidState() throws Exception {
    Matcher matcher;
    matcher = Patterns.findMatcher("123", ImmutableSet.of(Pattern.compile(".(.).")));
    assertNotNull(matcher);
    assertEquals("2", matcher.group(1));
  }

  public void testCapturingPattern_matches() throws Exception {
    assertTrue(Patterns.matchesAny("123", ImmutableSet.of(Pattern.compile(".(.)."))));
  }
}
