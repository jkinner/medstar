package com.sociodyne.parser.edi;

public interface ParserFactory<T extends Parser> {
	T create(EdiLocation location, Tokenizer tokenizer, EdiHandler handler);
}
