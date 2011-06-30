package com.sociodyne.validation.edi;

import com.sociodyne.test.Mock;
import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.ContentHandler;

import com.google.common.collect.ImmutableMap;

public class SegmentParserTest extends MockEdiParserTest {
	@Mock(Mock.Type.NICE) ElementParser elementParser;
	@Mock(Mock.Type.NICE) ParserFactory<ElementParser> elementParserFactory;

	// Elements are all removed; element separator token is to trigger
	// recursive descent.
	private static final String SHORT_ISA_HEADER =
		"ISA*~";

	public void setUp() throws Exception {
		super.setUp();
		expect(elementParserFactory.create(anyObject(Reader.class), anyObject(Configuration.class),
				anyObject(ContentHandler.class), anyObject(Location.class)))
				.andReturn(elementParser);
	}

	public void testParseIsaSegment() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream(SHORT_ISA_HEADER.getBytes()));
		expectStartSegment("ISA");

		expect(elementParser.parse()).andReturn((int)'~');

		expectEndSegment();

		replay();
		
		SegmentParser segmentParser = createSegmentParser(reader);
		segmentParser.parse();
	}

	private SegmentParser createSegmentParser(Reader reader) {
		SegmentParser segmentParser =
			new SegmentParser(reader,
				configuration, location, contentHandler, new EdiReader.Context(),
				ImmutableMap.<String, ParserFactory<? extends ElementParser>>of(),
				elementParserFactory);
		return segmentParser;
	}

	public void testParseEmptySegment_succeeds() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("".getBytes()));

		replay();
		
		SegmentParser segmentParser = createSegmentParser(reader);
		segmentParser.parse();
	}

	public void testParseIncompleteSegment_throwsEOFException() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("ISA".getBytes()));

		replay();
		
		SegmentParser segmentParser = createSegmentParser(reader);
		try {
			segmentParser.parse();
			fail("Expected EOFException");
		} catch (EOFException e) {
			// Expected
		}
	}

	public void testParseMultipleSegmentsNoElements_succeeds() throws Exception {
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("ISA~EB~".getBytes()));

		expectStartSegment("ISA");
		expectEndSegment();
		expectStartSegment("EB");
		expectEndSegment();
		
		replay();

		SegmentParser segmentParser = createSegmentParser(reader);
		segmentParser.parse();
	}

	public void testParseMultipleSegmentsWithElements_succeeds() throws Exception {
		// Important: The segment terminators are "read" by the mock element parser
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("ISA*EB*".getBytes()));

		expectStartSegment("ISA");
		expect(elementParser.parse()).andReturn((int)'~');
		expectEndSegment();
		expectStartSegment("EB");
		expect(elementParser.parse()).andReturn((int)'~');
		expectEndSegment();
		
		replay();

		SegmentParser segmentParser = createSegmentParser(reader);
		segmentParser.parse();
	}

}
