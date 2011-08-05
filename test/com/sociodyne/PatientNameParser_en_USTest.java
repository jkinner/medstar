package com.sociodyne;

import junit.framework.TestCase;


/**
 * Tests for {@link PatientNameParser_en_US}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class PatientNameParser_en_USTest extends TestCase {
  public void testEmptyName_throwsIllegalArgumentException() throws Exception {
    try {
      new PatientNameParser_en_US().valueOf("");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  public void testOneName_throwsIllegalArgumentException() throws Exception {
    try {
      new PatientNameParser_en_US().valueOf("Jason");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  public void testHonorificOneName_throwsNullPointerException() throws Exception {
    try {
      new PatientNameParser_en_US().valueOf("Mr. Jason");
      fail("Expected IllegalArgumentException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  public void testFirstAndLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason Kinner");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
  }

  public void testFirstAndLastNameSuffix_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason Kinner Jr.");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals("Jr.", name.getSuffixes().get(0));
  }

  public void testFirstAndLastNameMultipleSuffix_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason Kinner Jr., Esq.");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals("Jr.", name.getSuffixes().get(0));
    assertEquals("Esq.", name.getSuffixes().get(1));
  }

  public void testHonorificFirstAndLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Mr. Jason Kinner");
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
  }

  public void testHonorificNoPeriodFirstAndLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Mr Jason Kinner");
    assertEquals("Mr", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
  }

  public void testHonorificFirstAndLastNameSuffix_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Mr. Jason Kinner Jr.");
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals("Jr.", name.getSuffixes().get(0));
  }

  public void testHonorificFirstAndLastNameMultipleSuffix_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Mr. Jason Kinner Jr., Esq.");
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals("Jr.", name.getSuffixes().get(0));
    assertEquals("Esq.", name.getSuffixes().get(1));
  }


  public void testFirstMiLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason A. Kinner");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals((Character)'A', name.getMiddleInitial());
    assertNull(name.getMiddleName());
  }

  public void testFirstMiWithNoPeriodLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason A Kinner");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals((Character)'A', name.getMiddleInitial());
    assertNull(name.getMiddleName());
  }


  public void testFirstMiddleLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason Allen Kinner");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals((Character)'A', name.getMiddleInitial());
    assertEquals("Allen", name.getMiddleName());
  }

  public void testFirstMultipleMiddleLastName_succeeds() throws Exception {
    PatientName name = new PatientNameParser_en_US().valueOf("Jason Allen Grier Kinner");
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals((Character)'A', name.getMiddleInitial());
    assertEquals("Allen Grier", name.getMiddleName());
  }
}
