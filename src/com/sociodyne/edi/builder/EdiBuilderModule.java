// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Binds classes required for {@link EdiBuilder} and its sub-classes to function.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class EdiBuilderModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bind the ISA sequence generator in Singleton scope so it increases with each document
    bind(Key.get(Integer.class, Segments.named("ISA")))
        .toProvider(TransientSequenceGenerator.class)
        .in(Singleton.class);
    bind(Key.get(Integer.class, Segments.named("GS")))
        .toProvider(TransientSequenceGenerator.class);
    bind(Key.get(Integer.class, Segments.named("ST")))
        .toProvider(TransientSequenceGenerator.class);

    // Install the factories for each gateway
    install(new FactoryModuleBuilder()
        .implement(EdiBuilder.class, CmsEdiBuilder.class)
        .build(Key.get(EdiBuilderFactory.class, Gateways.forGateway(Gateways.CMS))));

  }
}
