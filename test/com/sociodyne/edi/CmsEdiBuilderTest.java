package com.sociodyne.edi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import com.sociodyne.parser.edi.Configuration;
import com.sociodyne.parser.edi.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import com.google.inject.Provider;


public class CmsEdiBuilderTest extends MockTest {
  @Mock Provider<Integer> isaSequenceGenerator;
  @Mock Provider<Integer> gsSequenceGenerator;
  @Mock Provider<Integer> stSequenceGenerator;
  @Mock(Mock.Type.STRICT) EdiHandler handler;
  Configuration configuration = Configuration.builder()
      .setElementSeparator('*')
      .setSubElementSeparator(':')
      .setSegmentTerminator('~')
      .build();

  public void testValidationRequest_setsStIdentifier() throws Exception {
    expect(isaSequenceGenerator.get()).andReturn(1);
    expect(gsSequenceGenerator.get()).andReturn(1);
    expect(stSequenceGenerator.get()).andReturn(1);

    handler.startSegment("ISA");
    expectAnyElementTimes(16);
    handler.endSegment();
    handler.startSegment("GS");
    expectAnyElementTimes(8);
    handler.endSegment();
    handler.startSegment("ST");
    handler.startElement("270");
    handler.endElement();
    handler.startElement(anyObject(String.class));
    handler.endElement();
    handler.endSegment();
    handler.startSegment("SE");
    expectAnyElementTimes(2);
    handler.endSegment();
    handler.startSegment("GE");
    expectAnyElementTimes(2);
    handler.endSegment();
    handler.startSegment("IEA");
    expectAnyElementTimes(2);
    handler.endSegment();

    replay();

    CmsEdiBuilder builder = new CmsEdiBuilder(isaSequenceGenerator, gsSequenceGenerator,
        stSequenceGenerator, handler, DocumentType.VALIDATION_REQUEST);
    builder.setStage(IsaSegmentBuilder.Stage.TEST);
    builder.setFunctionalId("HS");
    builder.setVersionIndustryReleaseCode("004010X092A1");

    builder.startDocument();
    builder.endDocument();
  }
  
  private void expectAnyElementTimes(int times) throws Exception {
    for (int i = 0; i < times; i++) {
      handler.startElement(anyObject(String.class));
      handler.endElement();
    }
  }
}
