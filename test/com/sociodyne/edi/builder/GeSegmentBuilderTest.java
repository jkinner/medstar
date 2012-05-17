// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.sociodyne.edi.parser.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;


/**
 * Tests for {@link GeSegmentBuilder}.
 * 
 * @author jkinner@sociodyne.com
 */
public class GeSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testAllFieldsSet_controlNumberPadding() throws Exception {
    expect(ediBuilder.getGsSequenceNumber()).andReturn(123);
    expect(ediBuilder.getGsSequenceCount()).andReturn(1);

    handler.startSegment(eq("GE"));
    handler.startElement(eq("1"));
    handler.startElement(eq("000000123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    GeSegmentBuilder builder = new GeSegmentBuilder(ediBuilder);
    builder.setPadControlNumber(true);
    builder.build(handler);
  }
  
  public void testAllFieldsSet_noControlNumberPadding() throws Exception {
    expect(ediBuilder.getGsSequenceNumber()).andReturn(123);
    expect(ediBuilder.getGsSequenceCount()).andReturn(1);

    handler.startSegment(eq("GE"));
    handler.startElement(eq("1"));
    handler.startElement(eq("123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    GeSegmentBuilder builder = new GeSegmentBuilder(ediBuilder);
    builder.setPadControlNumber(false);
    builder.build(handler);
  }

}
