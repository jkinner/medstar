package com.sociodyne.validation.edi;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.common.collect.Iterators;

import junit.framework.TestCase;

public class EdiReaderTransfomerTest extends TestCase {
	private static final String VALID_ISA_HEADER =
		"ISA*00*        *00*        *ZZ*T000000011  *ZZ*CMS *"
		 + "050516*0734*U*00401*000005014*1*P*:~";
	private static final String SHORT_ISA_HEADER =
		"ISA****************:~";

	private static final class EdiNamespaceContext implements NamespaceContext {
		public String getNamespaceURI(String prefix) {
			return (prefix.equals("") || prefix.equals("edi"))?EdiReader.NAMESPACE_URI:null;
		}

		public String getPrefix(String namespaceUri) {
			return namespaceUri.equals(EdiReader.NAMESPACE_URI)?"":null;
		}

		public Iterator<String> getPrefixes(String namespaceUri) {
			return namespaceUri.equals(EdiReader.NAMESPACE_URI)?
					Iterators.forArray(new String[] { "", "edi" })
					:Iterators.<String>emptyIterator();
		}
	}

	private static final Transformer transformer;
	private static final XPath xpath;
	private static final NamespaceContext namespaceContext;

	static {
		TransformerFactory tf = TransformerFactory.newInstance();
		namespaceContext = new EdiNamespaceContext();
		try {
			transformer = tf.newTransformer();
			XPathFactory f = XPathFactory.newInstance();
			xpath = f.newXPath();
			xpath.setNamespaceContext(namespaceContext);
		} catch (TransformerConfigurationException e) {
			System.err.println("Unable to create JAXP Transformer");
			throw new RuntimeException(e);
		}
	}

	public void testParseSegment_withElements_succeeds() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(VALID_ISA_HEADER.getBytes());
		DOMResult output = new DOMResult();
		transformer.transform(new SAXSource(reader, new InputSource(is)), output);
//		transformer.transform(new DOMSource(output.getNode()), new StreamResult(System.out));
		
		Node root = output.getNode();
		Document doc = (Document) root;
		
		XPathExpression testSegmentType = xpath.compile("/edi:edi/edi:segment[@type = 'ISA']");
		Node n = (Node) testSegmentType.evaluate(doc, XPathConstants.NODE);
		assertNotNull("Expected an ISA segment", n);
		XPathExpression testCMS = xpath.compile("/edi:edi/edi:segment[@type = 'ISA']/edi:element[8]");
		n = (Node)testCMS.evaluate(output.getNode(), XPathConstants.NODE);
		assertNotNull("Expected an ISA element in ISA08", n);
		assertEquals("CMS ", n.getTextContent());
	}

	public void testParseSegment_noElements_succeeds() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
				("ISA~")
					.getBytes());
		DOMResult output = new DOMResult();
		transformer.transform(new SAXSource(reader, new InputSource(is)), output);
//		transformer.transform(new DOMSource(output.getNode()), new StreamResult(System.out));
		
		Node root = output.getNode();
		Document doc = (Document) root;
		
		XPathExpression testSegmentType = xpath.compile("/edi:edi/edi:segment[@type = 'ISA']");
		Node n = (Node) testSegmentType.evaluate(doc, XPathConstants.NODE);
		assertNotNull("Expected an ISA segment", n);
	}

	public void testParseSegment_noTerminator_throwsEOFException() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
				("ISA")
					.getBytes());
		DOMResult output = new DOMResult();
		try {
			transformer.transform(new SAXSource(reader, new InputSource(is)), output);
			fail("Expected EOFException");
		} catch (TransformerException e) {
			assertEquals(EOFException.class, e.getCause().getClass());
		}
	}

	public void testParseSegment_withElementsNoTerminator_throwsEOFException() throws Exception {
		EdiReader reader = new EdiReader();
		ByteArrayInputStream is = new ByteArrayInputStream(
				("ISA*00*ZZ*")
					.getBytes());
		DOMResult output = new DOMResult();
		try {
			transformer.transform(new SAXSource(reader, new InputSource(is)), output);
			fail("Expected EOFException");
		} catch (TransformerException e) {
			assertEquals(EOFException.class, e.getCause().getClass());
		}
	}

}
