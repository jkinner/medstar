// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

public interface ParserFactory<T extends Parser> {

  T create(EdiLocation location, Tokenizer tokenizer, EdiHandler handler);
}
