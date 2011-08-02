package com.sociodyne.edi.builder;

import com.sociodyne.edi.Configuration;
import com.sociodyne.edi.parser.EdiHandler;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Provides the envelope for talking to CMS (the Medicare insurance gateway).
 *
 * @author jkinner@sociodyne.com (Jason Kinner)
 * @see <a href="http://www.cms.gov/HETSHelp/downloads/HETS270271CompanionGuide.pdf">
        HETS/CMS Companion Guide</a>
 */
public class CmsEdiBuilder extends EdiBuilder {
  private static final String NO_ID_QUALIFIER = "ZZ";
  private static final String NO_INFO_QUALIFIER = "00";
  private static final String NO_RESPONSIBLE_AGENCY = "X";
  // TODO(jkinner): Replace with real value
  private static final String SOCIODYNE_SENDER_ID = "SOCIODYNE_";
  private static final String CMS_RECEIVER_ID = "CMS";
  private static final Configuration CMS_CONFIGURATION = Configuration.builder()
      .setElementSeparator('*')
      .setSubElementSeparator('|')
      .setSegmentTerminator('~').build();

  @Inject
  CmsEdiBuilder(@SegmentNamed("ISA") Provider<Integer> isaSequenceGenerator,
      @SegmentNamed("GS") Provider<Integer> gsSequenceGenerator,
      @SegmentNamed("ST") Provider<Integer> stSequenceGenerator,
      @Assisted EdiHandler handler, @Assisted DocumentType type) {
    super(isaSequenceGenerator, gsSequenceGenerator, stSequenceGenerator, handler,
        CMS_CONFIGURATION, type);
    isaSegmentBuilder.setAuthorizeInfoQualifier(NO_INFO_QUALIFIER)
        .setAuthorizeInfo(Strings.repeat(" ", 10))
        .setSecurityInfoQualifier(NO_INFO_QUALIFIER)
        .setSecurityInfo(Strings.repeat(" ", 10))
        .setInterchangeSenderIdQualifier(NO_ID_QUALIFIER)
        .setInterchangeSenderId(SOCIODYNE_SENDER_ID)
        .setInterchangeReceiverIdQualifier(NO_ID_QUALIFIER)
        .setInterchangeReceiverId(Strings.padEnd(CMS_RECEIVER_ID, 15, ' '))
        .setAcknowledgeRequested(false);

    stSegmentBuilder
        .setPadControlNumber(true);

    gsSegmentBuilder.setPadControlNumber(false)
        .setApplicationSenderCode(SOCIODYNE_SENDER_ID)
        .setApplicationReceiverCode(CMS_RECEIVER_ID)
        .setResponsibleAgencyCode(NO_RESPONSIBLE_AGENCY);

    geSegmentBuilder.setPadControlNumber(false);
    seSegmentBuilder.setPadControlNumber(true);
  }
}
