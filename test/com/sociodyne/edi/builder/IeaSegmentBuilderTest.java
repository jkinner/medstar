// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.sociodyne.edi.parser.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;


/**
 * Tests for {@link IeaSegmentBuilder}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class IeaSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testBuildsSegment() throws Exception {
    expect(ediBuilder.getIsaSequenceNumber()).andReturn(123);

    handler.startSegment(eq("IEA"));
    handler.startElement(eq("1"));
    handler.startElement(eq("000000123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    IeaSegmentBuilder builder = new IeaSegmentBuilder(ediBuilder);

    builder.build(handler);
  }
}
