package com.sociodyne.edi.builder;

import com.sociodyne.edi.EdiException;
import com.sociodyne.edi.parser.EdiHandler;

import com.google.common.base.Strings;

/**
 * Builds the "end interaction" ({@code IEA}) segment for a given {@link EdiBuilder}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class IeaSegmentBuilder {
  private final EdiBuilder ediBuilder;
  
  public IeaSegmentBuilder(EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  public void build(EdiHandler handler) throws EdiException {

    String controlNumber = Strings.padStart(Integer.toString(ediBuilder.getIsaSequenceNumber()),
        9, '0');

    String fields[] = {
        // TODO(jkinner): Find out how to (optionally) transmit multiple functional groups
        "1",
        controlNumber,
    };

    handler.startSegment("IEA");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}
