package com.sociodyne.parser.edi;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ParserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SegmentParserFactory.class);

    // This is only used by SegmentParserFactory
    install(new FactoryModuleBuilder().build(new TypeLiteral<ParserFactory<SegmentParser>>() {
    }));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ParserFactory<ElementListParser>>() {
    }));
    install(new FactoryModuleBuilder()
        .build(new TypeLiteral<ParserFactory<SubElementListParser>>() {
        }));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ParserFactory<SegmentListParser>>() {
    }));
    bind(SegmentParserFactory.class);
  }

}
