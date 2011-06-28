package com.sociodyne.validation.edi;

import com.sociodyne.test.Mock;

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SegmentParserTest extends MockEdiParserTest {
	@Mock(Mock.Type.NICE) ElementParser elementParser;

	// Elements are all removed; element separator token is to trigger
	// recursive descent.
	private static final String SHORT_ISA_HEADER =
		"ISA*~";
	private static final String ISA_HEADER_WITH_SUBELEMENT =
		"ISA****************:~";

	public void testParseIsaSegment() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream(SHORT_ISA_HEADER.getBytes()));
		expectStartSegment("ISA");

		expect(elementParser.parse()).andReturn((int)'~');

		expectEndSegment();

		replay();
		
		SegmentParser segmentParser = new SegmentParser(reader, configuration, location,
				contentHandler, elementParser);
		segmentParser.parse();
	}

	public void testParseEmptySegment_succeeds() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("".getBytes()));

		replay();
		
		SegmentParser segmentParser = new SegmentParser(reader, configuration, location,
				contentHandler, elementParser);
		segmentParser.parse();
	}

	public void testParseIncompleteSegment_throwsEOFException() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("ISA".getBytes()));

		replay();
		
		SegmentParser segmentParser = new SegmentParser(reader, configuration, location,
				contentHandler, elementParser);
		try {
			segmentParser.parse();
			fail("Expected EOFException");
		} catch (EOFException e) {
			// Expected
		}
	}

	public void testParseMultipleSegmentsNoElements_succeeds() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream("ISA~EB~".getBytes()));

		expectStartSegment("ISA");
		expectEndSegment();
		expectStartSegment("EB");
		expectEndSegment();
		
		replay();

		SegmentParser segmentParser = new SegmentParser(reader, configuration, location,
				contentHandler, elementParser);
		segmentParser.parse();
	}

	public void testParseMultipleSegmentsWithElements_succeeds() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
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

		SegmentParser segmentParser = new SegmentParser(reader, configuration, location,
				contentHandler, elementParser);
		segmentParser.parse();
	}

}
