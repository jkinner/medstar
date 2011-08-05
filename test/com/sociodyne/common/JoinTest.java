package com.sociodyne.common;

import junit.framework.TestCase;

import com.google.common.collect.ImmutableSet;


public class JoinTest extends TestCase {
  public void testEmptyList() throws Exception {
    assertEquals("", Join.join(" ", ImmutableSet.<String>of()));
  }

  public void testOneItemList() throws Exception {
    assertEquals("1", Join.join(" ", ImmutableSet.<String>of("1")));
  }

  public void testTwoItemList() throws Exception {
    assertEquals("1 2", Join.join(" ", ImmutableSet.<String>of("1", "2")));
  }

  public void testComplexSeparator_withEmptyList() throws Exception {
    assertEquals("", Join.join("', '", ImmutableSet.<String>of()));
  }

  public void testComplexSeparator_withOneItemList() throws Exception {
    assertEquals("1", Join.join("', '", ImmutableSet.<String>of("1")));
  }

  public void testComplexSeparator_withTwoItemList() throws Exception {
    assertEquals("1', '2", Join.join("', '", ImmutableSet.<String>of("1", "2")));
  }
}
