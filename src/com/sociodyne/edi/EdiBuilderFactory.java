package com.sociodyne.edi;

import com.sociodyne.parser.edi.EdiHandler;


public interface EdiBuilderFactory {
  EdiBuilder create(EdiHandler handler, DocumentType type);
}
