package com.sociodyne;

import junit.framework.TestCase;


/**
 * Tests for {@link Strings} utility class.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class StringsTest extends TestCase {
  
  public void testPadLeft_emptyString_padsFullString() throws Exception {
    StringBuffer resultBuffer = new StringBuffer();
    Strings.padLeft("", '0', 8, resultBuffer, 8);
    assertEquals("00000000", resultBuffer.toString());
  }

  public void testPadLeft_oneCharString_padsLeft() throws Exception {
    StringBuffer resultBuffer = new StringBuffer();
    Strings.padLeft("1", '0', 8, resultBuffer, 8);
    assertEquals("00000001", resultBuffer.toString());
  }

  public void testPadLeft_fullString_noPadding() throws Exception {
    StringBuffer resultBuffer = new StringBuffer();
    Strings.padLeft("11111111", '0', 8, resultBuffer, 8);
    assertEquals("11111111", resultBuffer.toString());
  }

  public void testPadLeft_bigString_throwsIllegalArgumentException() throws Exception {
    StringBuffer resultBuffer = new StringBuffer();
    try {
      Strings.padLeft("111111111", '0', 8, resultBuffer, 8);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  public void testPadLeft_bigString_noMaxSize_noPadding() throws Exception {
    StringBuffer resultBuffer = new StringBuffer();
    Strings.padLeft("111111111", '0', 8, resultBuffer);
    assertEquals("111111111", resultBuffer.toString());
  }
}
