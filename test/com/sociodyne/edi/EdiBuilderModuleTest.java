package com.sociodyne.edi;

import com.sociodyne.parser.edi.EdiHandler;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;


public class EdiBuilderModuleTest extends MockTest {
  @Mock EdiHandler handler;

  public void testCreateCmsGateway() throws Exception {
    replay();

    Injector ediBuilderInjector = Guice.createInjector(new EdiBuilderModule());
    EdiBuilderFactory cmsBuilderFactory =
        ediBuilderInjector.getInstance(Key.get(EdiBuilderFactory.class, Gateways.forGateway(Gateways.CMS)));
    EdiBuilder cmsBuilder = cmsBuilderFactory.create(handler,
        DocumentType.VALIDATION_REQUEST);
    assertNotNull(cmsBuilder);
  }
}
