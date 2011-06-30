package com.sociodyne.validation.edi;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SubElementParserTest extends MockEdiParserTest {
	EdiReader.Location location;
	EdiReader.Configuration configuration;

	public void setUp() throws Exception {
		super.setUp();
		location = new EdiReader.Location();
		configuration = new EdiReader.Configuration();
	}

	public void testParseEmptySubElements_succeeds() throws Exception {
		expectStartSubElement("");
		expectEndSubElement();

		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream("*".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler);
		assertEquals('*', (int)parser.parse());
	}

	public void testParseSubElements_noTerminator_throwsEofException() throws Exception {
		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream("".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler);
		try {
			parser.parse();
			fail("Expected EOFException");
		} catch (EOFException e) {
			// Expected
		}
		
	}
	public void testParseEmptySubElements_terminatedBySegmentTerminator_succeeds() throws Exception {
		expectStartSubElement("");
		expectEndSubElement();

		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream("~".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler);
		assertEquals('~', (int)parser.parse());
	}

	public void testParseMultipleSubElements_succeeds() throws Exception {
		expectStartSubElement("");
		expectEndSubElement();
		expectStartSubElement("");
		expectEndSubElement();

		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream("|*".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler);
		assertEquals('*', (int)parser.parse());
	}

	public void testParseMultipleSubElements_alternateSeparator_succeeds() throws Exception {
		configuration.setSubElementSeparator(':');
		expectStartSubElement("");
		expectEndSubElement();
		expectStartSubElement("");
		expectEndSubElement();

		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream(":*".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler);
		assertEquals('*', (int)parser.parse());
	}
}
