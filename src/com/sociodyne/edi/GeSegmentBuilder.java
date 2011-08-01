package com.sociodyne.edi;

import com.sociodyne.Strings;
import com.sociodyne.parser.edi.EdiException;
import com.sociodyne.parser.edi.EdiHandler;

import com.google.inject.Inject;

/**
 * Builds the "group end" ({@code GE}) segment for a particular {@link EdiBuilder}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class GeSegmentBuilder {
  private final EdiBuilder ediBuilder;
  private boolean padControlNumber;

  @Inject
  GeSegmentBuilder(final EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  /** Sets the option of left-padding the control number. */
  public void setPadControlNumber(final boolean padControlNumber) {
    this.padControlNumber = padControlNumber;
  }

  public void build(EdiHandler handler) throws EdiException {

    String controlNumber = Integer.toString(ediBuilder.getGsSequenceNumber());
    if (padControlNumber) {
      controlNumber = Strings.padLeft(controlNumber, '0', 9);
    }

    String fields[] = {
        Integer.toString(ediBuilder.getGsSequenceCount()),
        controlNumber
    };

    handler.startSegment("GE");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}
