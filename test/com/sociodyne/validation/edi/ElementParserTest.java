package com.sociodyne.validation.edi;

import static org.easymock.EasyMock.*;

import com.sociodyne.test.Mock;
import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.ContentHandler;

public class ElementParserTest extends MockEdiParserTest {
	private static final String SHORT_ISA_ELEMENTS =
		"***************:~";

	EdiReader.Location location;
	EdiReader.Configuration configuration;

	@Mock(Mock.Type.NICE) SubElementParser subElementParser;
	@Mock(Mock.Type.NICE) ParserFactory<SubElementParser> subElementParserFactory;

	public void setUp() throws Exception {
		super.setUp();
		location = new EdiReader.Location();
		configuration = new EdiReader.Configuration();
		expect(subElementParserFactory.create(anyObject(Reader.class),
				anyObject(Configuration.class), anyObject(ContentHandler.class),
				anyObject(Location.class))).andReturn(subElementParser);
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
				contentHandler, subElementParserFactory);
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
				contentHandler, subElementParserFactory);
		parser.parse();
		assertEquals(':', configuration.getSubElementSeparator());
	}

}
