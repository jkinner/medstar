package com.sociodyne.edi;

import com.sociodyne.Strings;
import com.sociodyne.parser.edi.EdiException;
import com.sociodyne.parser.edi.EdiHandler;

import java.util.Calendar;

import com.google.common.base.Preconditions;


public class GsSegmentBuilder {

  private String functionalId;
  private String applicationSenderCode;
  private String applicationReceiverCode;
  private Calendar created;
  private String responsibleAgencyCode;
  private String versionIndustryReleaseCode;

  private final EdiBuilder ediBuilder;

  // Configuration
  private boolean padControlNumber;
  
  public GsSegmentBuilder(EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  public GsSegmentBuilder setPadControlNumber(boolean padControlNumber) {
    this.padControlNumber = padControlNumber;
    return this;
  }

  public GsSegmentBuilder setApplicationSenderCode(String applicationSenderCode) {
    this.applicationSenderCode = applicationSenderCode;
    return this;
  }

  public GsSegmentBuilder setApplicationReceiverCode(String applicationReceiverCode) {
    this.applicationReceiverCode = applicationReceiverCode;
    return this;
  }

  public GsSegmentBuilder setCreated(Calendar created) {
    this.created = created;
    return this;
  }

  public GsSegmentBuilder setFunctionalId(String functionalId) {
    this.functionalId = functionalId;
    return this;
  }

  public GsSegmentBuilder setResponsibleAgencyCode(String responsibleAgencyCode) {
    this.responsibleAgencyCode = responsibleAgencyCode;
    return this;
  }
  
  public GsSegmentBuilder setVersionIndustryReleaseCode(String versionIndustryReleaseCode) {
    this.versionIndustryReleaseCode = versionIndustryReleaseCode;
    return this;
  }

  public void build(EdiHandler handler) throws EdiException {
    Preconditions.checkState(created != null, "Created time must be set");
    Preconditions.checkState(functionalId != null, "Functional identifier must be set");
    Preconditions.checkState(applicationSenderCode != null, "Application sender code must be set");
    Preconditions.checkState(applicationReceiverCode != null,
        "Application receiver code must be set");
    Preconditions.checkState(responsibleAgencyCode != null, "Responsible agency code must be set");
    Preconditions.checkState(versionIndustryReleaseCode != null,
        "Version/Industry/Release code must be set");

    StringBuffer createdDateBuffer = new StringBuffer();
    Strings.padLeft(Integer.toString(created.get(Calendar.YEAR)).substring(2, 4), '0', 2,
        createdDateBuffer, 2);
    Strings.padLeft(Integer.toString(created.get(Calendar.MONTH)), '0', 2, createdDateBuffer);
    Strings.padLeft(Integer.toString(created.get(Calendar.DAY_OF_MONTH)), '0', 2,
        createdDateBuffer);

    StringBuffer createdTimeBuffer = new StringBuffer();
    Strings.padLeft(Integer.toString(created.get(Calendar.HOUR_OF_DAY)), '0', 2, createdTimeBuffer);
    Strings.padLeft(Integer.toString(created.get(Calendar.MINUTE)), '0', 2, createdTimeBuffer);

    String controlNumber = Integer.toString(ediBuilder.startGsSequence());
    if (padControlNumber) {
      controlNumber = Strings.padLeft(controlNumber, '0', 9);
    }

    String fields[] = {
        functionalId,
        applicationSenderCode,
        applicationReceiverCode,
        createdDateBuffer.toString(),
        createdTimeBuffer.toString(),
        controlNumber,
        responsibleAgencyCode,
        versionIndustryReleaseCode
    };

    handler.startSegment("GS");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}
