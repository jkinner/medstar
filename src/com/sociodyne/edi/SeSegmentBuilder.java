package com.sociodyne.edi;

import com.sociodyne.Strings;
import com.sociodyne.parser.edi.EdiException;
import com.sociodyne.parser.edi.EdiHandler;


public class SeSegmentBuilder {
  private final EdiBuilder ediBuilder;
  private boolean padControlNumber;

  public SeSegmentBuilder(final EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  public void setPadControlNumber(final boolean padControlNumber) {
    this.padControlNumber = padControlNumber;
  }

  public void build(EdiHandler handler)
      throws EdiException {

    String controlNumber = Integer.toString(ediBuilder.getStSequenceNumber());
    if (padControlNumber) {
      controlNumber = Strings.padLeft(controlNumber, '0', 9);
    }

    String fields[] = {
        Integer.toString(ediBuilder.getStSegmentCount()),
        controlNumber
    };

    handler.startSegment("SE");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}