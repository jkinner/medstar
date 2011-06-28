package com.sociodyne.validation.edi;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class SubElementParserTest extends MockEdiParserTest {
	EdiReader.Location location;
	EdiReader.Configuration configuration;
	private static final Map<ImmutableEdiLocation, ValueTransformer<String, String>>
		EMPTY_TRANSFORMERS = ImmutableMap.<ImmutableEdiLocation, ValueTransformer<String, String>>of();

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
				contentHandler, EMPTY_TRANSFORMERS);
		assertEquals('*', (int)parser.parse());
	}

	public void testParseSubElements_noTerminator_throwsEofException() throws Exception {
		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream("".getBytes()));
		SubElementParser parser = new SubElementParser(reader, configuration, location,
				contentHandler, EMPTY_TRANSFORMERS);
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
				contentHandler, EMPTY_TRANSFORMERS);
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
				contentHandler, EMPTY_TRANSFORMERS);
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
				contentHandler, EMPTY_TRANSFORMERS);
		assertEquals('*', (int)parser.parse());
	}
}
