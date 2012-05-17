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
public class StSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testNoFieldsSet_throwsIllegalStateException() throws Exception {
    replay();

    StSegmentBuilder builder = new StSegmentBuilder(ediBuilder);
    try {
      builder.build(handler);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  public void testAllFieldsSet_controlNumberPadding() throws Exception {
    expect(ediBuilder.startStSequence()).andReturn(123);

    handler.startSegment(eq("ST"));
    handler.startElement(eq("270"));
    handler.startElement(eq("000000123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, 7);
    calendar.set(Calendar.DAY_OF_MONTH, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 36);

    StSegmentBuilder builder = new StSegmentBuilder(ediBuilder)
      .setPadControlNumber(true)
      .setIdentifierCode("270");

    builder.build(handler);
  }
  
  public void testAllFieldsSet_noControlNumberPadding() throws Exception {
    expect(ediBuilder.startStSequence()).andReturn(123);

    handler.startSegment(eq("ST"));
    handler.startElement(eq("270"));
    handler.startElement(eq("123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2011);
    calendar.set(Calendar.MONTH, 7);
    calendar.set(Calendar.DAY_OF_MONTH, 26);
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 36);

    StSegmentBuilder builder = new StSegmentBuilder(ediBuilder)
    .setPadControlNumber(false)
    .setIdentifierCode("270");

    builder.build(handler);
  }

}
