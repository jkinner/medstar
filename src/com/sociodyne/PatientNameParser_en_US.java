// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne;

import com.sociodyne.common.Join;
import com.sociodyne.common.Patterns;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Parses a string to produce a {@link PatientName} compatible with the en_US locale (US English).
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class PatientNameParser_en_US implements PatientNameParser {
  private static final Set<String> HONORIFICS = ImmutableSet.of(
      "Mr.", "Mr",
      "Ms.", "Ms",
      "Miss",
      "Mrs.", "Mrs",
      "Dr.", "Dr",
      "Rev.", "Rev",  // Reverend
      "Fr.", "Fr",    // Father
      "Sr.", "Sr",    // Sister
      "Adv.", "Adv"   // Advocate
  );

  private static final Set<Pattern> SUFFIX_REGEXPS = ImmutableSet.of(
      Pattern.compile("^Sr\\.?$"),  // Senior
      Pattern.compile("^Jr\\.?$"),  // Junior
      Pattern.compile("^[IV]+$"),   // Roman numeral (< 10, should be good)
      Pattern.compile("^Esq\\.?$"), // Esquire
      Pattern.compile("^PhD\\.?$"),
      Pattern.compile("^([A-Z]+\\.)+[A-Z]+\\.?$") // Catch degree names
  );

  public PatientName valueOf(String name) {
    String[] parts = name.split("  *");

    if (parts.length < 2) {
      throw new IllegalArgumentException(name);
    }

    PatientName.Builder builder = PatientName.builder();
    List<String> unresolvedNameParts = Lists.newArrayList();
    Iterator<String> namePartIterator = Iterators.forArray(parts);

    String namePart = null;

    while (namePartIterator.hasNext()) {
      namePart = cleanNamePart(namePartIterator.next());
      if (HONORIFICS.contains(namePart)) {
        if (! builder.hasHonorific()) {
          builder.setHonorific(namePart);
        } else {
          // Skip
          continue;
        }
      } else {
        break;
      }
    }

    if (namePart != null) {
      namePart.replaceAll(",", "");
      if (namePart.length() > 0) {
        builder.setGivenName(namePart);
        namePart = null;
      }
    }

    while (!builder.hasGivenName() && namePartIterator.hasNext()) {
      namePart = cleanNamePart(namePartIterator.next());
      if (namePart.length() > 0) {
        builder.setGivenName(namePartIterator.next());
        namePart = null;
        break;
      }
    }

    while (namePartIterator.hasNext()) {
      namePart = cleanNamePart(namePartIterator.next());
      if (Patterns.matchesAny(namePart, SUFFIX_REGEXPS)) {
        break;
      }
      unresolvedNameParts.add(namePart);
      namePart = null;
    }

    // At this point, we've either encountered the end of the name parts
    // or the first suffix.
    if (unresolvedNameParts.size() > 0) {
      builder.setFamilyName(unresolvedNameParts.remove(unresolvedNameParts.size() - 1));
      if (unresolvedNameParts.size() > 0) {
        if (unresolvedNameParts.size() == 1) {
          // See if it's a middle initial
          String possibleMiddleInitial = unresolvedNameParts.get(0);
          if (possibleMiddleInitial.length() == 1
              && Character.isLetter(possibleMiddleInitial.charAt(0))) {
            builder.setMiddleInitial(possibleMiddleInitial.charAt(0));
          } else if (possibleMiddleInitial.length() == 2
              && possibleMiddleInitial.charAt(1) == '.') {
            builder.setMiddleInitial(possibleMiddleInitial.charAt(0));
          } else {
            builder.setMiddleName(possibleMiddleInitial);
          }
        } else {
          builder.setMiddleName(Join.join(" ", unresolvedNameParts));
        }
      }
    }

    if (namePart != null) {
      builder.addSuffix(namePart);
      namePart = null;
    }

    while (namePartIterator.hasNext()) {
      builder.addSuffix(namePartIterator.next());
    }
    
    return builder.build();
  }

  private String cleanNamePart(String namePart) {
    return namePart.replaceAll(",", "");
  }
}
