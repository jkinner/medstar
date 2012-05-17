// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.Configuration;
import com.sociodyne.edi.EdiException;

/**
 * Captures the sub-element separator from the ISA segment.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class IsaEdiHandler implements EdiHandler {

  private static final int ISA_SUBELEMENT_SEPARATOR_ELEMENT = 16;

  private final Configuration configuration;
  private final EdiHandler delegate;
  int currentElement = 0;

  public IsaEdiHandler(Configuration configuration, EdiHandler delegate) {
    this.configuration = configuration;
    this.delegate = delegate;
  }

  public void startSegment(String segmentIdentifier) throws EdiException {
    delegate.startSegment(segmentIdentifier);
  }

  public void endSegment() throws EdiException {
    delegate.endSegment();
  }

  public void startElement(String contents) throws EdiException {
    currentElement++;
    if (currentElement == ISA_SUBELEMENT_SEPARATOR_ELEMENT) {
      configuration.setSubElementSeparator(contents.charAt(0));
    }
    delegate.startElement(contents);
  }

  public void endElement() throws EdiException {
    delegate.endElement();
  }

  public void subElement(String contents) throws EdiException {
    delegate.subElement(contents);
  }

  public void startLoop(String segmentIdentifier) throws EdiException {
    delegate.startLoop(segmentIdentifier);
  }

  public void endLoop() throws EdiException {
    delegate.endLoop();
  }
}
