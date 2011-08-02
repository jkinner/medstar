package com.sociodyne.edi.builder;

import com.sociodyne.Strings;
import com.sociodyne.edi.EdiException;
import com.sociodyne.edi.parser.EdiHandler;

import com.google.common.base.Preconditions;

/**
 * Builds the "sequence start" ({@code ST}) EDI segment for a given {@link EdiBuilder}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class StSegmentBuilder {

  private String identifierCode;

  private final EdiBuilder ediBuilder;

  // Configuration
  private boolean padControlNumber;
  
  public StSegmentBuilder(final EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  public StSegmentBuilder setPadControlNumber(final boolean padControlNumber) {
    this.padControlNumber = padControlNumber;
    return this;
  }

  public StSegmentBuilder setIdentifierCode(String identifierCode) {
    this.identifierCode = identifierCode;
    return this;
  }

  public void build(EdiHandler handler)
      throws EdiException {
    Preconditions.checkState(identifierCode != null, "Identifier code must be set");

    String controlNumber = Integer.toString(ediBuilder.startStSequence());
    if (padControlNumber) {
      controlNumber = Strings.padLeft(controlNumber, '0', 9);
    }

    String fields[] = {
        identifierCode,
        controlNumber,
    };

    handler.startSegment("ST");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}
