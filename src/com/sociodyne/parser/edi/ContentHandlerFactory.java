package com.sociodyne.parser.edi;

import org.xml.sax.ContentHandler;

public interface ContentHandlerFactory {

  ContentHandler create(Configuration configuration);
}
