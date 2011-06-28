package com.sociodyne.validation.edi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MockEdiParserTest extends MockTest {
	@Mock(Mock.Type.STRICT) protected ContentHandler contentHandler;

	protected void expectStartSegment(String type) throws SAXException {
		EdiReader.EdiAttributes ediAttributes = new EdiReader.EdiAttributes();
		ediAttributes.put(new QName("", "type", ""), type);

		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("segment"), eq("segment"),
				eq(ediAttributes));
	}

	protected void expectEndSegment() throws SAXException {
		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("segment"), eq("segment"));
	}

	protected void expectStartElement() throws SAXException {
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("element"), eq("element"),
				anyObject(Attributes.class));
	}

	protected void expectStartElement(String content) throws SAXException {
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("element"), eq("element"),
				anyObject(Attributes.class));
		contentHandler.characters(aryEq(content.toCharArray()), eq(0), eq(content.length()));
	}

	protected void expectEndElement() throws SAXException {
		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("element"), eq("element"));
	}

	protected void expectSimpleElement() throws SAXException {
		expectStartElement();
		expectEndElement();
	}

	protected void expectSimpleElement(String content) throws SAXException {
		expectStartElement(content);
		expectEndElement();
	}
	
	protected void expectStartSubElement(String content) throws SAXException {
		contentHandler.startElement(eq(EdiConstants.NAMESPACE_URI), eq("subelement"), eq("subelement"),
				anyObject(Attributes.class));
		contentHandler.characters(aryEq(content.toCharArray()), eq(0), eq(content.length()));
	}

	protected void expectEndSubElement() throws SAXException {
		contentHandler.endElement(eq(EdiConstants.NAMESPACE_URI), eq("subelement"), eq("subelement"));
	}
}
