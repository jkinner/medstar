package com.sociodyne.parser.edi;

import java.io.EOFException;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ElementListParser implements Parser {
	private final Tokenizer tokenizer;
	private final ParserFactory<SubElementListParser> subElementListParserFactory;
	private final EdiLocation location;
	private final EdiHandler handler;

	@Inject
	public ElementListParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location,
			@Assisted EdiHandler handler,
			ParserFactory<SubElementListParser> subElementListParserFactory) {
		this.tokenizer = tokenizer;
		this.subElementListParserFactory = subElementListParserFactory;
		this.location = location;
		this.handler = handler;
	}

	public boolean matches(Token token) {
		return token.getType() == Token.Type.ELEMENT_SEPARATOR;
	}

	public Token parse(Token token) throws EdiException, IOException {
		if (!matches(token)) {
			throw new EdiException("Expected ELEMENT_SEPARATOR, found " + token);
		}

		while (true) {
			token = tokenizer.nextToken();
			if (token == null) {
				throw new EOFException();
			}

			ELEMENT_TOKEN_TYPE:
			switch(token.getType()) {
			case WORD:
				// Good
				location.nextElement();
				handler.startElement(token.getValue());
				token = tokenizer.nextToken();
				if (token == null) {
					throw new EOFException();
				}

				switch (token.getType()) {
				case SUB_ELEMENT_SEPARATOR:
					// Descend
					SubElementListParser subParser = subElementListParserFactory.create(location,
							tokenizer, handler);
					// Cause this token to become part of this parse.
					Token subToken = subParser.parse(token);
					// Check terminal
					switch (subToken.getType()) {
					case ELEMENT_SEPARATOR:
						// Good. Continue.
						handler.endElement();
						break;
					case SEGMENT_TERMINATOR:
						handler.endElement();
						return subToken;
					default:
						throw new EdiException("Expected ELEMENT_SEPARATOR or SEGMENT_TERMINATOR, got " + subToken);
					}
					break ELEMENT_TOKEN_TYPE;
				case ELEMENT_SEPARATOR:
					// Good
					handler.endElement();
					break ELEMENT_TOKEN_TYPE;
				case SEGMENT_TERMINATOR:
					handler.endElement();
					return token;
				default:
					throw new EdiException("Expected WORD or ELEMENT_SEPARATOR, found " + token);
				}
			case ELEMENT_SEPARATOR:
				handler.startElement("");
				handler.endElement();
				break;
			case SEGMENT_TERMINATOR:
				break;
			default:
				throw new UnexpectedTokenException(token);
			}
		}
	}
}
