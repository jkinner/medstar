package com.sociodyne.xml;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Chain of content filters, each of which is invoked when a new SAX event is received.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class ContentFilterChain extends DelegatingContentHandler {
  private LinkedList<ChainedContentHandler> filters = Lists.newLinkedList();

  public void add(ChainedContentHandler filter) {
    Preconditions.checkNotNull(filter);

    if (filters.size() > 0) {
      // Sets the delegate for the last filter in the chain to the filter being added.
      filters.getLast().setNext(filter);
    } else {
      // Sets the delegate to the first filter in the chain.
      next = filter;
    }

    filters.add(filter);
  }

  public void addAll(List<? extends ChainedContentHandler> filters) {
    Preconditions.checkNotNull(filters);

    for (ChainedContentHandler filter : filters) {
      add(filter);
    }
  }

  public static ContentFilterChain of(ChainedContentHandler... filters) {
    ContentFilterChain filterChain = new ContentFilterChain();
    
    for (ChainedContentHandler filter : filters) {
      filterChain.add(filter);
    }
    
    return filterChain;
  }
}
