package com.sociodyne.edi.parser;

import static org.easymock.EasyMock.expect;

import com.sociodyne.edi.Configuration;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import java.io.Writer;

public class EdiWriterTest extends MockTest {

  @Mock
  Writer writer;

  Configuration configuration = new Configuration.Builder().setSegmentTerminator('~')
      .setElementSeparator('*').setSubElementSeparator('|').build();

  public void testWriteSegment() throws Exception {
    expect(writer.append("ISA")).andReturn(writer);
    expect(writer.append('~')).andReturn(writer);

    replay();

    final EdiWriter ediWriter = new EdiWriter(configuration, writer);
    ediWriter.startSegment("ISA");
    ediWriter.endSegment();
  }

  public void testWriteElement() throws Exception {
    expect(writer.append('*')).andReturn(writer);
    expect(writer.append("ABC")).andReturn(writer);

    replay();

    final EdiWriter ediWriter = new EdiWriter(configuration, writer);
    ediWriter.startElement("ABC");
    ediWriter.endElement();
  }

  public void testWriteSubElement() throws Exception {
    expect(writer.append('|')).andReturn(writer);
    expect(writer.append("ABC")).andReturn(writer);

    replay();

    final EdiWriter ediWriter = new EdiWriter(configuration, writer);
    ediWriter.subElement("ABC");
  }

  public void testWriteSegment_noElements() throws Exception {
    expect(writer.append("ISA")).andReturn(writer);
    expect(writer.append('*')).andReturn(writer);
    expect(writer.append("ABC")).andReturn(writer);
    expect(writer.append('~')).andReturn(writer);

    replay();

    final EdiWriter ediWriter = new EdiWriter(configuration, writer);
    ediWriter.startSegment("ISA");
    ediWriter.startElement("ABC");
    ediWriter.endElement();
    ediWriter.endSegment();
  }
}
