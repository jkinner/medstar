package com.sociodyne.validation.edi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EdiReaderTest extends MockEdiParserTest {
	// Note that these examples overide the default setting of '|' for the sub-element separator
	private static final String VALID_ISA_HEADER =
		"ISA*00*        *00*        *ZZ*T000000011  *ZZ*CMS *"
		 + "050516*0734*U*00401*000005014*1*P*:~";
	private static final String SHORT_ISA_HEADER =
		"ISA****************:~";

	public void testParseShortIsaSegment_succeeds() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
			SHORT_ISA_HEADER.getBytes());
		contentHandler.startDocument();
		contentHandler.startPrefixMapping("", EdiConstants.NAMESPACE_URI);
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"),
			anyObject(Attributes.class));
		// expectShortISA()
		expectShortIsaSegment();
		// end: expectShortISA()
		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"));
		contentHandler.endDocument();

		replay();
		reader.setContentHandler(contentHandler);
		reader.parse(new InputSource(is));
	}

	private void expectRealIsaSegment() throws SAXException {
		expectStartSegment("ISA");
		expectSimpleElement("00");
		expectSimpleElement("        ");
		expectSimpleElement("00");
		expectSimpleElement("        ");
		expectSimpleElement("ZZ");
		expectSimpleElement("T000000011  ");
		expectSimpleElement("ZZ");
		expectSimpleElement("CMS ");
		expectSimpleElement("050516");
		expectSimpleElement("0734");
		expectSimpleElement("U");
		expectSimpleElement("00401");
		expectSimpleElement("000005014");
		expectSimpleElement("1");
		expectSimpleElement("P");
		expectSimpleElement(":");
		expectEndSegment();
	}

	public void testParseRealIsaSegment_succeeds() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
			VALID_ISA_HEADER.getBytes());
		contentHandler.startDocument();
		contentHandler.startPrefixMapping("", EdiConstants.NAMESPACE_URI);
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"),
			anyObject(Attributes.class));
		expectRealIsaSegment();
		// end: expectShortISA()
		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"));
		contentHandler.endDocument();

		replay();
		reader.setContentHandler(contentHandler);
		reader.parse(new InputSource(is));
	}

	private void expectShortIsaSegment() throws SAXException {
		expectStartSegment("ISA");
		for (int i = 0; i < 15; i++) {
			expectSimpleElement();
		}
		expectSimpleElement(":");
		expectEndSegment();
	}

	public void testParseSegment_withSubElements_succeeds() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
			(SHORT_ISA_HEADER + "EB*D*IND**MB*********HC:G0389~").getBytes());
		contentHandler.startDocument();
		contentHandler.startPrefixMapping("", EdiConstants.NAMESPACE_URI);
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"),
			anyObject(Attributes.class));

		expectShortIsaSegment();
		
		expectStartSegment("EB");
		expectSimpleElement("D");
		expectSimpleElement("IND");
		expectSimpleElement();
		expectSimpleElement("MB");
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectSimpleElement();
		expectStartElement("HC");
		expectStartSubElement("G0389");
		expectEndSubElement();
		expectEndElement();
		expectEndSegment();

		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("edi"), eq("edi"));
		contentHandler.endDocument();
		replay();
		reader.setContentHandler(contentHandler);
		reader.parse(new InputSource(is));
	}



}
