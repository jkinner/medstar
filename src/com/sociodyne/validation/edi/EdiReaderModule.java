package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.ElementParser.NoSubElementsParser;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

public class EdiReaderModule extends AbstractModule {

	@Override
	protected void configure() {
		// Top-level segment parser factory
		install(new FactoryModuleBuilder()
			.implement(SegmentParser.class, SegmentParser.class)
			.build(new TypeLiteral<ParserFactory<SegmentParser>>() {})
		);

		// Top-level element parser factory
		install(new FactoryModuleBuilder()
			.implement(ElementParser.class, ElementParser.class)
			.build(new TypeLiteral<ParserFactory<ElementParser>>() {})
		);

		// Top-level sub-element parser factory
		install(new FactoryModuleBuilder()
			.implement(SubElementParser.class, SubElementParser.class)
			.build(new TypeLiteral<ParserFactory<SubElementParser>>() {})
		);

		install(new FactoryModuleBuilder()
			.implement(NoSubElementsParser.class, NoSubElementsParser.class)
			.build(new TypeLiteral<ParserFactory<NoSubElementsParser>>() {})
		);

		// Special parser factories
		// HL Parser
		install(new FactoryModuleBuilder()
			.implement(ElementParser.class, HLParser.class)
			.build(new TypeLiteral<ParserFactory<HLParser>>() {})
		);

		// LS/LE Parser
		install(new FactoryModuleBuilder()
			.implement(ElementParser.class, LoopParser.class)
			.build(new TypeLiteral<ParserFactory<LoopParser>>() {})
		);
		install(new FactoryModuleBuilder()
			.implement(ElementParser.class, LoopEndParser.class)
			.build(new TypeLiteral<ParserFactory<LoopEndParser>>() {})
		);

		MapBinder<String, ParserFactory<? extends ElementParser>> elementParserFactoryBinder =
			MapBinder.newMapBinder(binder(), new TypeLiteral<String> () {},
					new TypeLiteral<ParserFactory<? extends ElementParser>>() {});
		elementParserFactoryBinder
			.addBinding("HL")
			.to(new TypeLiteral<ParserFactory<HLParser>>() {});
		elementParserFactoryBinder
			.addBinding("LS")
			.to(new TypeLiteral<ParserFactory<LoopParser>>() {});
		elementParserFactoryBinder
			.addBinding("LE")
			.to(new TypeLiteral<ParserFactory<LoopEndParser>>() {});

	}

	@Provides EdiReader.Context providesEdiReaderContext() {
		// TODO(jkinner): Use our own scope instead of a {@code ThreadLocal}?
		return EdiReader.getContext();
	}
}
