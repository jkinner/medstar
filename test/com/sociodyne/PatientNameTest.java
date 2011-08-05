package com.sociodyne;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

/**
 * Tests for {@link PatientName}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class PatientNameTest extends TestCase {

  public void testSimpleName_returnsAllParts() {
    final PatientName name = new PatientName("Mr.", "Jason", null, null, "Kinner",
        ImmutableList.<String>of());
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Kinner", name.getFamilyName());
    assertNull(name.getMiddleName());
    assertNull(name.getMiddleInitial());
    assertEquals(0, name.getSuffixes().size());
  }

  public void testFullName_returnsAllParts() {
    final PatientName name = new PatientName("Mr.", "Jason", "Allen", 'A', "Kinner",
        ImmutableList.<String>of());
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Allen", name.getMiddleName());
    assertEquals('A', (char) name.getMiddleInitial());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals(0, name.getSuffixes().size());
  }

  public void testFullNameNoMiddleInitial_returnsAllParts() {
    final PatientName name = new PatientName("Mr.", "Jason", "Allen", null, "Kinner",
        ImmutableList.<String>of());
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Allen", name.getMiddleName());
    assertEquals('A', (char) name.getMiddleInitial());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals(0, name.getSuffixes().size());
  }

  public void testFullNameMiddleInitialOnly_returnsAllParts() {
    final PatientName name = new PatientName("Mr.", "Jason", null, 'A', "Kinner",
        ImmutableList.<String>of());
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertNull(name.getMiddleName());
    assertEquals('A', (char) name.getMiddleInitial());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals(0, name.getSuffixes().size());
  }

  public void testCompleteName_returnsAllParts() {
    final PatientName name = new PatientName("Mr.", "Jason", "Allen", null, "Kinner",
        ImmutableList.of("Esq.", "III"));
    assertEquals("Mr.", name.getHonorific());
    assertEquals("Jason", name.getGivenName());
    assertEquals("Allen", name.getMiddleName());
    assertEquals('A', (char) name.getMiddleInitial());
    assertEquals("Kinner", name.getFamilyName());
    assertEquals(2, name.getSuffixes().size());
    assertEquals("Esq.", name.getSuffixes().get(0));
    assertEquals("III", name.getSuffixes().get(1));
  }
}
