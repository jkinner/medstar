package com.sociodyne.xml;


/**
 * Defines a filter that is a {@code ContentHandler} that also forwards the SAX event
 * to the next content filter in the chain. Implementors will typically override one or
 * more methods and they MUST forward the call to the {@code next} member except when {@code next}
 * is {@code  null}. Overridden methods MAY change the value, create a new value, etc. and forward
 * the new content to the {@code next} filter in the chain.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class ChainedContentHandlerBase extends DelegatingContentHandler implements
    ChainedContentHandler {

  public void setNext(ChainedContentHandler next) {
    this.next = next;
  }
}
