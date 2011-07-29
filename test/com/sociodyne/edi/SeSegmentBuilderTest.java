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
public class SeSegmentBuilderTest extends MockTest {
  @Mock EdiBuilder ediBuilder;
  @Mock EdiHandler handler;

  public void testAllFieldsSet_controlNumberPadding() throws Exception {
    expect(ediBuilder.getStSequenceNumber()).andReturn(123);
    expect(ediBuilder.getStSegmentCount()).andReturn(1);

    handler.startSegment(eq("SE"));
    handler.startElement(eq("1"));
    handler.startElement(eq("000000123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    SeSegmentBuilder builder = new SeSegmentBuilder(ediBuilder);
    builder.setPadControlNumber(true);
    builder.build(handler);
  }
  
  public void testAllFieldsSet_noControlNumberPadding() throws Exception {
    expect(ediBuilder.getStSequenceNumber()).andReturn(123);
    expect(ediBuilder.getStSegmentCount()).andReturn(2);

    handler.startSegment(eq("SE"));
    handler.startElement(eq("2"));
    handler.startElement(eq("123"));
    handler.endElement();
    expectLastCall().times(2);
    handler.endSegment();

    replay();

    SeSegmentBuilder builder = new SeSegmentBuilder(ediBuilder);
    builder.setPadControlNumber(false);
    builder.build(handler);
  }

}
