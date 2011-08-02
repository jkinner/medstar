package com.sociodyne.edi.parser;

import com.sociodyne.edi.Configuration;

import org.xml.sax.ContentHandler;

public interface ContentHandlerFactory {

  ContentHandler create(Configuration configuration);
}
