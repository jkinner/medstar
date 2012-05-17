// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.EdiException;

import java.io.IOException;

public interface Parser {

  boolean matches(Token token);

  Token parse(Token startToken) throws EdiException, IOException;

}
