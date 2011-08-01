package com.sociodyne.edi;

import com.sociodyne.parser.edi.EdiHandler;

/**
 * Constructs a Guice-configured EdiBuilder.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public interface EdiBuilderFactory {
  EdiBuilder create(EdiHandler handler, DocumentType type);
}
