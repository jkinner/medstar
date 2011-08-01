package com.sociodyne.edi;

import com.sociodyne.Strings;
import com.sociodyne.parser.edi.EdiException;
import com.sociodyne.parser.edi.EdiHandler;

import java.util.Calendar;

import com.google.common.base.Preconditions;

/**
 * Builds an {@code ISA} segment, the start of the EDI envelope.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class IsaSegmentBuilder {

  private String authorizeInfoQualifier;
  private String authorizeInfo;
  private String securityInfoQualifier;
  private String securityInfo;
  private String interchangeSenderIdQualifier;
  private String interchangeSenderId;
  private String interchangeReceiverIdQualifier;
  private String interchangeReceiverId;
  private Calendar created;
  private String interchangeStandardsId = "U";
  private String interchangeVersionId;
  private boolean acknowledgeRequested;
  private Stage stage;

  public enum Stage {
    TEST("T"),
    PRODUCTION("P");
    
    private final String indicator;

    private Stage(String indicator) {
      this.indicator = indicator;
    }

    public String getIndicator() {
      return indicator;
    }

    public static Stage valueOfIndicator(String indicator) {
      if (indicator.equals("T")) {
        return TEST;
      } else if (indicator.equals("P")) {
        return PRODUCTION;
      }
      throw new IllegalArgumentException(indicator);
    }
  }

  private final EdiBuilder ediBuilder;

  private char subElementSeparator;

  public IsaSegmentBuilder(EdiBuilder ediBuilder) {
    this.ediBuilder = ediBuilder;
  }
  
  public IsaSegmentBuilder setSubElementSeparator(char subElementSeparator) {
    this.subElementSeparator = subElementSeparator;
    return this;
  }

  public IsaSegmentBuilder setAuthorizeInfoQualifier(String authorizeInfoQualifier) {
    this.authorizeInfoQualifier = authorizeInfoQualifier;
    return this;
  }

  
  public IsaSegmentBuilder setAuthorizeInfo(String authorizeInfo) {
    this.authorizeInfo = authorizeInfo;
    return this;
  }

  
  public IsaSegmentBuilder setSecurityInfoQualifier(String securityInfoQualifier) {
    this.securityInfoQualifier = securityInfoQualifier;
    return this;
  }

  
  public IsaSegmentBuilder setSecurityInfo(String securityInfo) {
    this.securityInfo = securityInfo;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeSenderIdQualifier(String interchangeSenderIdQualifier) {
    this.interchangeSenderIdQualifier = interchangeSenderIdQualifier;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeSenderId(String interchangeSenderId) {
    this.interchangeSenderId = interchangeSenderId;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeReceiverIdQualifier(String interchangeReceiverIdQualifier) {
    this.interchangeReceiverIdQualifier = interchangeReceiverIdQualifier;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeReceiverId(String interchangeReceiverId) {
    this.interchangeReceiverId = interchangeReceiverId;
    return this;
  }

  
  public IsaSegmentBuilder setCreated(Calendar created) {
    this.created = created;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeStandardsId(String interchangeStandardsId) {
    this.interchangeStandardsId = interchangeStandardsId;
    return this;
  }

  
  public IsaSegmentBuilder setInterchangeVersionId(String interchangeVersionId) {
    this.interchangeVersionId = interchangeVersionId;
    return this;
  }

  
  public IsaSegmentBuilder setAcknowledgeRequested(boolean acknowledgeRequested) {
    this.acknowledgeRequested = acknowledgeRequested;
    return this;
  }

  
  public IsaSegmentBuilder setStage(Stage stage) {
    this.stage = stage;
    return this;
  }


  public void build(EdiHandler handler) throws EdiException {
    Preconditions.checkState(created != null, "Created time must be set");
    Preconditions.checkState(stage != null, "Stage (test or production) must be set");
    Preconditions.checkState(interchangeVersionId != null, "Interchange version ID must be set");

    StringBuffer createdDateBuffer = new StringBuffer();
    Strings.padLeft(Integer.toString(created.get(Calendar.YEAR)).substring(2, 4), '0', 2, createdDateBuffer, 2);
    Strings.padLeft(Integer.toString(created.get(Calendar.MONTH)), '0', 2, createdDateBuffer);
    Strings.padLeft(Integer.toString(created.get(Calendar.DAY_OF_MONTH)), '0', 2,
        createdDateBuffer);

    StringBuffer createdTimeBuffer = new StringBuffer();
    Strings.padLeft(Integer.toString(created.get(Calendar.HOUR_OF_DAY)), '0', 2, createdTimeBuffer);
    Strings.padLeft(Integer.toString(created.get(Calendar.MINUTE)), '0', 2, createdTimeBuffer);

    String controlNumber = Strings.padLeft(Integer.toString(ediBuilder.getIsaSequenceNumber()),
        '0', 9);

    String fields[] = {
        authorizeInfoQualifier == null?"":authorizeInfoQualifier,
        authorizeInfo == null?"":authorizeInfo,
        securityInfoQualifier == null?"":securityInfoQualifier,
        securityInfo == null?"":securityInfo,
        interchangeSenderIdQualifier == null?"":interchangeSenderIdQualifier,
        interchangeSenderId == null?"":interchangeSenderId,
        interchangeReceiverIdQualifier == null?"":interchangeReceiverIdQualifier,
        interchangeReceiverId == null?"":interchangeReceiverId,
        createdDateBuffer.toString(),
        createdTimeBuffer.toString(),
        interchangeStandardsId == null?"":interchangeStandardsId,
        interchangeVersionId,
        controlNumber,
        acknowledgeRequested?"1":"0",
        stage.getIndicator(),
        Character.toString(subElementSeparator)
    };

    handler.startSegment("ISA");
    for (String field : fields) {
      handler.startElement(field);
      handler.endElement();
    }
    handler.endSegment();
  }
}
