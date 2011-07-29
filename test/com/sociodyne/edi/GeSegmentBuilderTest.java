package com.sociodyne.edi;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.sociodyne.parser.edi.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;


/**
 * Tests for {@link IsaSegmentBuilder}.
 * 
 * @author jkinner@sociodyne.com
 */
public class GeSegmentBuilderTest extends MockTest {
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
    expect(ediBuilder.getGsSequenceNumber()).andReturn(123);
    expect(ediBuilder.getGsSequenceCount()).andReturn(1);

    handler.startSegment(eq("GE"));
    handler.startElement(eq("1"));
    handler.startElement(eq("000000123"));
    handler.endElement();
    expectLastCall().times(2);

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

    replay();

    GeSegmentBuilder builder = new GeSegmentBuilder(ediBuilder);
    builder.setPadControlNumber(false);
    builder.build(handler);
  }

}
