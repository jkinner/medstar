package com.sociodyne.validation.edi;

import static org.easymock.EasyMock.expect;

import com.sociodyne.test.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ElementParserTest extends MockEdiParserTest {
	private static final String SHORT_ISA_ELEMENTS =
		"***************:~";

	EdiReader.Location location;
	EdiReader.Configuration configuration;
	private static final Map<ImmutableEdiLocation, ValueTransformer<String, String>>
		EMPTY_TRANSFORMERS = ImmutableMap.<ImmutableEdiLocation, ValueTransformer<String, String>>of();

	@Mock(Mock.Type.NICE) SubElementParser subElementParser;
	
	public void setUp() throws Exception {
		super.setUp();
		location = new EdiReader.Location();
		configuration = new EdiReader.Configuration();
	}

	public void testParseShortIsaHeader_succeeds() throws Exception {
		for (int i = 0; i < 15; i++) {
			expectStartElement();
			expectEndElement();
		}
		expectStartElement(":");
		expectEndElement();

		replay();
		Reader reader = new InputStreamReader(new ByteArrayInputStream(SHORT_ISA_ELEMENTS.getBytes()));
		ElementParser parser = new ElementParser(reader, configuration, location,
				contentHandler, subElementParser, EMPTY_TRANSFORMERS);
		parser.parse();
	}

	public void testParseIsaSegment_setsSubElement() throws Exception {
		EdiReader.Location location = new EdiReader.Location();
		EdiReader.Configuration configuration = new EdiReader.Configuration();
		Reader reader =
			new InputStreamReader(new ByteArrayInputStream(SHORT_ISA_ELEMENTS.getBytes()));
		for (int i = 0; i < 15; i++) {
			expectSimpleElement();
		}
		expectSimpleElement(":");

		replay();
		
		ElementParser parser = new ElementParser(reader, configuration, location,
				contentHandler, subElementParser, EMPTY_TRANSFORMERS);
		parser.parse();
		assertEquals(':', configuration.getSubElementSeparator());
	}

}
