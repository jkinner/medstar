package com.sociodyne.xml;

import org.xml.sax.ContentHandler;


public interface ChainedContentHandler extends ContentHandler {
  void setNext(ChainedContentHandler next);
}
