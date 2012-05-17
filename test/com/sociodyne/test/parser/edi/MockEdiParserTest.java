// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.test.parser.edi;

import com.sociodyne.edi.parser.EdiHandler;
import com.sociodyne.edi.parser.EdiLocation;
import com.sociodyne.edi.parser.Token;
import com.sociodyne.edi.parser.Tokenizer;
import com.sociodyne.parser.Location;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import org.easymock.EasyMock;

public class MockEdiParserTest extends MockTest {

  @Mock
  protected Tokenizer tokenizer;
  @Mock
  protected EdiHandler handler;
  @Mock(Mock.Type.NICE)
  protected EdiLocation location;
  @Mock(Mock.Type.NICE)
  protected Location fileLocation;

  protected void readTokens(Token... tokens) throws Exception {
    for (final Token token : tokens) {
      EasyMock.expect(tokenizer.nextToken()).andReturn(token);
    }
    EasyMock.expect(tokenizer.nextToken()).andStubReturn(null);
  }
}
