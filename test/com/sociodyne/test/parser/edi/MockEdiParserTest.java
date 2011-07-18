package com.sociodyne.test.parser.edi;

import com.sociodyne.parser.edi.EdiHandler;
import com.sociodyne.parser.edi.EdiLocation;
import com.sociodyne.parser.edi.Token;
import com.sociodyne.parser.edi.Tokenizer;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import org.easymock.EasyMock;

public class MockEdiParserTest extends MockTest {
	@Mock protected Tokenizer tokenizer;
	@Mock protected EdiHandler handler;
	@Mock(Mock.Type.NICE) protected EdiLocation location;

	protected void readTokens(Token... tokens) throws Exception {
		for (int i = 0; i < tokens.length; i++) {
			EasyMock.expect(tokenizer.nextToken()).andReturn(tokens[i]);
		}
		EasyMock.expect(tokenizer.nextToken()).andStubReturn(null);
	}
}
