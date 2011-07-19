package com.sociodyne.parser.edi;

import java.io.IOException;
import java.io.Writer;

public class EdiWriter implements EdiHandler {

  private final Configuration configuration;
  private final Writer writer;
  private final EdiLocation location = new EdiLocation("ISA", 0);
  private final boolean wroteSubElementSeparator = false;

  public EdiWriter(Configuration configuration, Writer writer) {
    this.configuration = configuration;
    this.writer = writer;
  }

  public void startSegment(String segmentIdentifier) throws EdiException {
    location.startSegment(segmentIdentifier);
    append(segmentIdentifier);
  }

  public void endSegment() throws EdiException {
    location.endSegment();
    append(configuration.getSegmentTerminator());
  }

  public void startElement(String contents) throws EdiException {
    location.startElement();
    append(configuration.getElementSeparator());

    if (!wroteSubElementSeparator && location.getSegment().equals("ISA")
        && location.getIndex() == 16) {
      // Ignore whatever contents were provided; emit the sub-element separator
// instead.
      append(configuration.getSubElementSeparator());
    } else {
      append(contents);
    }
  }

  public void endElement() throws EdiException {
    location.endElement();
  }

  public void subElement(String contents) throws EdiException {
    append(configuration.getSubElementSeparator());
    append(contents);
  }

  public void startLoop(String segmentIdentifier) throws EdiException {
  }

  public void endLoop() throws EdiException {
  }

  private void append(char character) throws EdiException {
    try {
      writer.append(character);
    } catch (final IOException ioe) {
      throw new EdiException(ioe);
    }
  }

  private void append(String contents) throws EdiException {
    try {
      writer.append(contents);
    } catch (final IOException ioe) {
      throw new EdiException(ioe);
    }
  }
}
