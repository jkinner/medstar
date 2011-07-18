package com.sociodyne.parser.edi;

import java.io.EOFException;
import java.io.IOException;

import com.google.inject.assistedinject.Assisted;

public class LsLoopParser extends SegmentParser {
	private static final Token LS_TOKEN = Token.word("LS");
	private final SegmentParserFactory segmentParserFactory;

	public LsLoopParser(@Assisted Tokenizer tokenizer, @Assisted EdiLocation location,
			@Assisted EdiHandler handler,
			ParserFactory<ElementListParser> elementListParserFactory,
			SegmentParserFactory segmentParserFactory) {
		super(tokenizer, location, handler, elementListParserFactory);
		this.segmentParserFactory = segmentParserFactory;
	}

	public boolean matches(Token token) {
		return token.equals(LS_TOKEN);
	}

	public Token parse(Token startToken) throws ParseException, IOException {
		if (!matches(startToken)) {
			throw new UnexpectedTokenException(startToken, LS_TOKEN);
		}

		String loopSegment = startToken.getValue();
		location.startSegment(loopSegment);
		handler.startLoop(loopSegment);
		handler.startSegment(loopSegment);

		Token token = tokenizer.nextToken();
		ElementListParser elementListParser = elementListParserFactory.create(location, tokenizer,
				handler);

		// LS requires elements, so let the element list parser complain if the parse fails.
		token = elementListParser.parse(token);
		if (token.getType() != Token.Type.SEGMENT_TERMINATOR) {
			throw new UnexpectedTokenException(token, Token.Type.SEGMENT_TERMINATOR);
		}

		String segmentIdentifier;
		token = tokenizer.nextToken();
		do {
			if (token == null) {
				throw new ParseException(new EOFException());
			}
			if (token.getType() != Token.Type.WORD) {
				throw new UnexpectedTokenException(token, Token.Type.WORD);
			}
			segmentIdentifier = token.getValue();
			SegmentParser segmentParser = segmentParserFactory.create(tokenizer, location, handler,
					token.getValue());
			token = segmentParser.parse(token);
			if (token == Token.SEGMENT_TERMINATOR) {
				token = tokenizer.nextToken();
			} else if (token.getType() != Token.Type.WORD) {
				throw new UnexpectedTokenException(token, Token.Type.WORD);
			}
			// Include the LE segment inside the LS loop.
		} while (!segmentIdentifier.equals("LE"));

		handler.endSegment();
		handler.endLoop();

		return token;
	}
}
