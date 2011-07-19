package com.sociodyne.parser.edi;

import java.io.IOException;

public interface Parser {

	boolean matches(Token token);

	Token parse(Token startToken) throws EdiException, IOException;

}