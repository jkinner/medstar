// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.builder;

import com.sociodyne.edi.parser.EdiHandler;

/**
 * Constructs a Guice-configured EdiBuilder.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public interface EdiBuilderFactory {
  EdiBuilder create(EdiHandler handler, DocumentType type);
}
