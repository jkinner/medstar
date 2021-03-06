// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import com.sociodyne.edi.EdiException;
import com.sociodyne.edi.parser.EdiHandler;

import java.util.Calendar;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Builds the "group start" ({@code GS}) segment for a given {@link EdiBuilder}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class GsSegmentBuilder {

  /** Type of request in the group (e.g. "HS" for a health-care status request) */
  private String functionalId;
  /** Application code of the sender of the request. Typically assigned by a gateway. */
  private String applicationSenderCode;
  /** Application code of the receiver of the request. Typically a gateway defines one of these. */
  private String applicationReceiverCode;
  /**
   * When this segment was created. {@code EdiBuilder} ensures this time is the same as the
   * {@code ISA} created time.
   */
  private Calendar created;
  private String responsibleAgencyCode;

  /**
   * Identifier for the specific EDI implementation that this document is intended to comply with.
   */
  private String versionIndustryReleaseCode;

  private final EdiBuilder ediBuilder;

  // Configuration
  /** Whether to left-pad the control number. */
  private boolean padControlNumber;
  
  public GsSegmentBuilder(EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }

  /** Sets whether to left-pad the sequence control number in the segment. */
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
    createdDateBuffer.append(
        Strings.padStart(Integer.toString(created.get(Calendar.YEAR)).substring(2, 4), 2, '0'));
    createdDateBuffer.append(
        Strings.padStart(Integer.toString(created.get(Calendar.MONTH)), 2, '0'));
    createdDateBuffer.append(
        Strings.padStart(Integer.toString(created.get(Calendar.DAY_OF_MONTH)), 2, '0'));

    StringBuffer createdTimeBuffer = new StringBuffer();
    createdTimeBuffer.append(
        Strings.padStart(Integer.toString(created.get(Calendar.HOUR_OF_DAY)), 2, '0'));
    createdTimeBuffer.append(
        Strings.padStart(Integer.toString(created.get(Calendar.MINUTE)), 2, '0'));

    String controlNumber = Integer.toString(ediBuilder.startGsSequence());
    if (padControlNumber) {
      controlNumber = Strings.padStart(controlNumber, 9, '0');
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
