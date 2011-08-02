package com.sociodyne.edi.builder;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.sociodyne.edi.parser.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import java.util.Calendar;


/**
 * Tests for {@link IsaSegmentBuilder}.
 * 
 * @author jkinner@sociodyne.com
 */
public class IsaSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testNoFieldsSet_throwsIllegalStateException() throws Exception {
    replay();

    IsaSegmentBuilder builder = new IsaSegmentBuilder(ediBuilder)
        .setSubElementSeparator(':');
    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testCreatedNotSet_throwsIllegalStateException() throws Exception {
    replay();

    IsaSegmentBuilder builder = new IsaSegmentBuilder(ediBuilder)
        .setSubElementSeparator(':')
        .setInterchangeVersionId("00401")
        .setStage(IsaSegmentBuilder.Stage.TEST);
    
    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testStageNotSet_throwsIllegalStateException() throws Exception {
    replay();

    IsaSegmentBuilder builder = new IsaSegmentBuilder(ediBuilder)
        .setSubElementSeparator(':')
        .setInterchangeVersionId("00401")
        .setCreated(Calendar.getInstance());

    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testInterchangeVersionIdNotSet_throwsIllegalStateException() throws Exception {
    replay();

    IsaSegmentBuilder builder = new IsaSegmentBuilder(ediBuilder)
        .setSubElementSeparator(':')
        .setStage(IsaSegmentBuilder.Stage.TEST)
        .setCreated(Calendar.getInstance());

    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testCreatedAndStageSet_buildsEmptySegment() throws Exception {
    expect(ediBuilder.getIsaSequenceNumber()).andReturn(123);

    handler.startSegment(eq("ISA"));
    handler.startElement(eq(""));
    expectLastCall().times(8);
    handler.startElement(eq("110726"));
    handler.startElement(eq("0836"));
    handler.startElement(eq("U"));
    handler.startElement(eq("00401"));
    handler.startElement(eq("000000123"));
    handler.startElement(eq("0"));
    handler.startElement(eq("T"));
    handler.startElement(eq(":"));
    handler.endElement();
    expectLastCall().times(16);
    handler.endSegment();

    replay();

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, 7);
    calendar.set(Calendar.DAY_OF_MONTH, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 36);

    IsaSegmentBuilder builder = new IsaSegmentBuilder(ediBuilder)
        .setSubElementSeparator(':')
        .setInterchangeVersionId("00401")
        .setStage(IsaSegmentBuilder.Stage.TEST)
        .setCreated(calendar);

    builder.build(handler);
  }
}
