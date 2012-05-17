// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Represents the full, complete name of a patient.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class PatientName {

  /** Honorific (Mr., Ms., Miss, Mrs., Dr.), including punctuation. */
  @Nullable
  private final String honorific;

  /** Given (first, in US) name */
  private final String givenName;

  /** Surname or family name (last, in US) */
  private final String familyName;

  /** Middle name. */
  @Nullable
  private final String middleName;

  /** Middle initial. */
  @Nullable
  private final Character middleInitial;

  /** Suffixes to the name (Esq., PhD, II, Sr. Jr.), including punctuation. */
  private final List<String> suffixes;

  public PatientName(@Nullable final String honorific, final String givenName,
      @Nullable final String middleName, @Nullable final Character middleInitial,
      String familyName, final List<String> suffixes) {
    Preconditions.checkNotNull(givenName, "givenName");
    Preconditions.checkNotNull(familyName, "familyName");
    Preconditions.checkNotNull(suffixes, "suffixes");

    this.honorific = honorific;
    this.givenName = givenName;
    this.middleName = middleName;
    if (middleInitial == null && middleName != null) {
      this.middleInitial = middleName.charAt(0);
    } else {
      if (middleName != null && middleInitial != middleName.charAt(0)) {
        throw new IllegalArgumentException("Middle initial '" + middleInitial
            + "' is not valid for middle name '" + middleName + "'");
      }
      this.middleInitial = middleInitial;
    }
    this.familyName = familyName;
    this.suffixes = suffixes;
  }

  public String getHonorific() {
    return honorific;
  }

  public String getGivenName() {
    return givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public Character getMiddleInitial() {
    return middleInitial;
  }

  public List<String> getSuffixes() {
    return suffixes;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o instanceof PatientName) {
      final PatientName that = (PatientName) o;
      return Objects.equal(honorific, that.honorific)
          && Objects.equal(givenName, that.givenName) && Objects.equal(middleName, that.middleName)
          && Objects.equal(middleInitial, that.middleInitial)
          && Objects.equal(familyName, that.familyName) && Objects.equal(suffixes, that.suffixes);
    }

    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("honorific", honorific).add("givenName", givenName)
        .add("middleName", middleName).add("middleIniital", middleInitial)
        .add("suffixes", suffixes).toString();
  }
  
  public static class Builder {
    private String honorific;
    private String givenName;
    private String middleName;
    private Character middleInitial;
    private String familyName;
    private List<String> suffixes = Lists.newArrayList();

    public Builder setHonorific(String honorific) {
      this.honorific = honorific;
      return this;
    }

    public boolean hasHonorific() {
      return honorific != null;
    }

    public Builder setGivenName(String givenName) {
      this.givenName = givenName;
      return this;
    }

    public boolean hasGivenName() {
      return givenName != null;
    }

    public Builder setMiddleName(String middleName) {
      this.middleName = middleName;
      return this;
    }
    
    public boolean hasMiddleName() {
      return middleName != null;
    }

    public Builder setMiddleInitial(char middleInitial) {
      this.middleInitial = middleInitial;
      return this;
    }

    public boolean hasMiddleInitial() {
      return middleInitial != null;
    }

    public Builder setFamilyName(String familyName) {
      this.familyName = familyName;
      return this;
    }

    public boolean hasFamilyName() {
      return familyName != null;
    }

    public Builder addSuffix(String suffix) {
      this.suffixes.add(suffix);
      return this;
    }
    
    public boolean hasSuffixes() {
      return suffixes.size() > 0;
    }

    public PatientName build() {
      return new PatientName(honorific, givenName, middleName, middleInitial, familyName, suffixes);
    }
  }
  
  public static Builder builder() {
    return new Builder();
  }
}
