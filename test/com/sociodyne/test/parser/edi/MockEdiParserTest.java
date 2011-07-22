package com.sociodyne.test.parser.edi;

import com.sociodyne.parser.Location;
import com.sociodyne.parser.edi.EdiHandler;
import com.sociodyne.parser.edi.EdiLocation;
import com.sociodyne.parser.edi.Token;
import com.sociodyne.parser.edi.Tokenizer;
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
