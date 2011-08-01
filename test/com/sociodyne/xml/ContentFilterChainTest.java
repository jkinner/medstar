package com.sociodyne.xml;

import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;
import com.sociodyne.xml.ChainedContentHandler;
import com.sociodyne.xml.ChainedContentHandlerBase;
import com.sociodyne.xml.ContentFilterChain;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * Tests for {@link ContentFilterChain}.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class ContentFilterChainTest extends MockTest {
  @Mock ChainedContentHandler[] filters = new ChainedContentHandler[3];
  ChainedContentHandlerBase[] handlers = new ChainedContentHandlerBase[3];

  public void setUp() throws Exception {
    super.setUp();
    for (int i = 0; i < handlers.length; i++) {
      handlers[i] = new MockCallingChainedContentHandler(filters[i]);
    }
  }

  public void testFiltersChainedTogether() throws Exception {
    filters[0].setNext(filters[1]);
    filters[1].setNext(filters[2]);

    replay();

    ContentFilterChain chain = new ContentFilterChain();
    chain.addAll(ImmutableList.copyOf(filters));
  }

  public void testFirstFilterCalled() throws Exception {
    filters[0].setNext(filters[1]);
    filters[1].setNext(filters[2]);

    filters[0].startDocument();

    replay();

    ContentFilterChain chain = new ContentFilterChain();
    chain.addAll(ImmutableList.copyOf(filters));
    
    
    chain.startDocument();
  }

  public void testAllFiltersCalled() throws Exception {
    for (int i = 0; i < filters.length; i++) {
      filters[i].startDocument();
    }

    replay();

    ContentFilterChain chain = new ContentFilterChain();
    chain.addAll(ImmutableList.copyOf(handlers));

    
    chain.startDocument();
  }

  private static class MockCallingChainedContentHandler extends ChainedContentHandlerBase {

    private final ChainedContentHandler mock;
    
    public MockCallingChainedContentHandler(ChainedContentHandler mock) {
      this.mock = mock;
    }

    @Override
    public void startDocument() throws SAXException {
      mock.startDocument();
      // Will call next.
      super.startDocument();
    }
  }
}
