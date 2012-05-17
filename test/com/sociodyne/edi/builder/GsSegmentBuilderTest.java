// Copyright 2011, Sociodyne LLC. All rights reserved.
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
public class GsSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testNoFieldsSet_throwsIllegalStateException() throws Exception {
    replay();

    GsSegmentBuilder builder = new GsSegmentBuilder(ediBuilder);
    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testAllFieldsSet_controlNumberPadding() throws Exception {
    expect(ediBuilder.startGsSequence()).andReturn(123);

    handler.startSegment(eq("GS"));
    handler.startElement(eq("HS"));
    handler.startElement(eq("XX"));
    handler.startElement(eq("YY"));
    handler.startElement(eq("110726"));
    handler.startElement(eq("0836"));
    handler.startElement(eq("000000123"));
    handler.startElement(eq("X"));
    handler.startElement(eq("004010X092A1"));
    handler.endElement();
    expectLastCall().times(8);
    handler.endSegment();

    replay();

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, 7);
    calendar.set(Calendar.DAY_OF_MONTH, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 36);

    GsSegmentBuilder builder = new GsSegmentBuilder(ediBuilder)
      .setPadControlNumber(true)
      .setFunctionalId("HS")
      .setApplicationSenderCode("XX")
      .setApplicationReceiverCode("YY")
      .setCreated(calendar)
      .setResponsibleAgencyCode("X")
      .setVersionIndustryReleaseCode("004010X092A1");

    builder.build(handler);
  }
  
  public void testAllFieldsSet_noControlNumberPadding() throws Exception {
    expect(ediBuilder.startGsSequence()).andReturn(123);

    handler.startSegment(eq("GS"));
    handler.startElement(eq("HS"));
    handler.startElement(eq("XX"));
    handler.startElement(eq("YY"));
    handler.startElement(eq("110726"));
    handler.startElement(eq("0836"));
    handler.startElement(eq("123"));
    handler.startElement(eq("X"));
    handler.startElement(eq("004010X092A1"));
    handler.endElement();
    expectLastCall().times(8);
    handler.endSegment();

    replay();

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, 7);
    calendar.set(Calendar.DAY_OF_MONTH, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 36);

    GsSegmentBuilder builder = new GsSegmentBuilder(ediBuilder)
      .setPadControlNumber(false)
      .setFunctionalId("HS")
      .setApplicationSenderCode("XX")
      .setApplicationReceiverCode("YY")
      .setCreated(calendar)
      .setResponsibleAgencyCode("X")
      .setVersionIndustryReleaseCode("004010X092A1");

    builder.build(handler);
  }

}
