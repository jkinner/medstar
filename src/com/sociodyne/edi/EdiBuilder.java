package com.sociodyne.edi;

import com.sociodyne.parser.edi.Configuration;
import com.sociodyne.parser.edi.EdiException;
import com.sociodyne.parser.edi.EdiHandler;

import java.util.Calendar;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Builds the envelope of an EDI document and accepts the contents as events on the
 * {@code EdiHandler} interface.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class EdiBuilder implements EdiHandler {
  private Integer isaSequence;
  private final Provider<Integer> isaSequenceGenerator;
  private final Provider<Integer> gsSequenceGenerator;
  private final Provider<Integer> stSequenceGenerator;
  protected final IsaSegmentBuilder isaSegmentBuilder;
  protected final GsSegmentBuilder gsSegmentBuilder;
  protected final StSegmentBuilder stSegmentBuilder;
  protected final IeaSegmentBuilder ieaSegmentBuilder;
  protected final GeSegmentBuilder geSegmentBuilder;
  protected final SeSegmentBuilder seSegmentBuilder;
  private Integer currentGsSequence;
  private int gsSequenceCount = 0;
  private Integer currentStSequence;
  private int stSegmentCount = 0;
  private final EdiHandler handler;
  private final Stack<String> segmentStack = new Stack<String>();

  protected final Configuration configuration;

  @Inject
  public EdiBuilder(Provider<Integer> isaSequenceGenerator, Provider<Integer> gsSequenceGenerator,
      Provider<Integer> stSequenceGenerator, EdiHandler handler, Configuration configuration,
      DocumentType type) {
    this.isaSequenceGenerator = isaSequenceGenerator;
    this.gsSequenceGenerator = gsSequenceGenerator;
    this.stSequenceGenerator = stSequenceGenerator;
    this.handler = handler;
    this.configuration = configuration;
    this.isaSegmentBuilder = new IsaSegmentBuilder(this);
    this.gsSegmentBuilder = new GsSegmentBuilder(this);
    this.stSegmentBuilder = new StSegmentBuilder(this); 
    this.ieaSegmentBuilder = new IeaSegmentBuilder(this);
    this.geSegmentBuilder = new GeSegmentBuilder(this);
    this.seSegmentBuilder = new SeSegmentBuilder(this);

    isaSegmentBuilder.setSubElementSeparator(configuration.getSubElementSeparator());
    stSegmentBuilder.setIdentifierCode(type.getDocumentIdentifierCode());
}

  public int getIsaSequenceNumber() {
    return isaSequence == null ? isaSequence = isaSequenceGenerator.get() : isaSequence;
  }

  public int startGsSequence() {
    gsSequenceCount++;
    return currentGsSequence = gsSequenceGenerator.get();
  }

  public int getGsSequenceCount() {
    return gsSequenceCount;
  }

  public int getGsSequenceNumber() {
    Preconditions.checkState(currentGsSequence != null, "No GS sequence has been started.");
    return currentGsSequence;
  }

  public int startStSequence() {
    return currentStSequence = stSequenceGenerator.get();
  }

  public int getStSegmentCount() {
    return stSegmentCount;
  }

  public int getStSequenceNumber() {
    Preconditions.checkState(currentStSequence != null, "No ST sequence has been started.");
    return currentStSequence;
  }
  
  public void setStage(IsaSegmentBuilder.Stage stage) {
    isaSegmentBuilder.setStage(stage);
  }

  public void setFunctionalId(String functionalId) {
    gsSegmentBuilder.setFunctionalId(functionalId);
  }

  public void setApplicationSenderCode(String applicationSenderCode) {
    gsSegmentBuilder.setApplicationSenderCode(applicationSenderCode);
  }

  public void setApplicationReceiverCode(String applicationReceiverCode) {
    gsSegmentBuilder.setApplicationReceiverCode(applicationReceiverCode);
  }

  public void setVersionIndustryReleaseCode(String versionIndustryReleaseCode) {
    gsSegmentBuilder.setVersionIndustryReleaseCode(versionIndustryReleaseCode);
  }

  public void setResponsibleAgencyCode(String responsibleAgencyCode) {
    gsSegmentBuilder.setResponsibleAgencyCode(responsibleAgencyCode);
  }

  public void startDocument() throws EdiException {
    Calendar created = Calendar.getInstance();
    isaSegmentBuilder.setCreated(created);
    // TODO(jkinner): Enum for interchange version ID?
    isaSegmentBuilder.setInterchangeVersionId("00401");
    gsSegmentBuilder.setCreated(created);

    isaSegmentBuilder.build(handler);
    gsSegmentBuilder.build(handler);
    stSegmentBuilder.build(handler);
  }

  public void endDocument() throws EdiException {
    seSegmentBuilder.build(handler);
    geSegmentBuilder.build(handler);
    ieaSegmentBuilder.build(handler);
  }

  public void startSegment(String segmentIdentifier) throws EdiException {
    if (segmentStack.isEmpty()) {
      stSegmentCount++;
    }
    handler.startSegment(segmentIdentifier);
  }

  public void endSegment() throws EdiException {
    handler.endSegment();
  }

  public void startElement(String contents) throws EdiException {
    handler.startElement(contents);
  }

  public void endElement() throws EdiException {
    handler.endElement();
  }

  public void subElement(String contents) throws EdiException {
    handler.subElement(contents);
  }

  public void startLoop(String segmentIdentifier) throws EdiException {
    if (segmentStack.isEmpty()) {
      stSegmentCount++;
    }

    segmentStack.push(segmentIdentifier);
    handler.startLoop(segmentIdentifier);
  }

  public void endLoop() throws EdiException {
    segmentStack.pop();
    handler.endLoop();
  }
}
