// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

public interface EdiHandler {

  void startSegment(String segmentIdentifier) throws EdiException;

  void endSegment() throws EdiException;

  void startElement(String contents) throws EdiException;

  void endElement() throws EdiException;

  void subElement(String contents) throws EdiException;

  void startLoop(String segmentIdentifier) throws EdiException;

  void endLoop() throws EdiException;
}
