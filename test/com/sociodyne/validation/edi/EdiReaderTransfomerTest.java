package com.sociodyne.validation.edi;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
			return (prefix.equals("") || prefix.equals("edi"))?EdiConstants.NAMESPACE_URI:null;
		}

		public String getPrefix(String namespaceUri) {
			return namespaceUri.equals(EdiConstants.NAMESPACE_URI)?"":null;
		}

		public Iterator<String> getPrefixes(String namespaceUri) {
			return namespaceUri.equals(EdiConstants.NAMESPACE_URI)?
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
		EdiReader reader = EdiReader.Factory.create();
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
		EdiReader reader = EdiReader.Factory.create();
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
		EdiReader reader = EdiReader.Factory.create();
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
		EdiReader reader = EdiReader.Factory.create();
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

	public void testParseSampleDocument_succeeds() throws Exception {
		String doc = "ISA*00*          *00*          *ZZ*wr2FGts2       *ZZ*wr2FGts2       *110120*0754*U*00401*237042407*0*P*^~GS*HB*MEDX*LLX1210001*20110120*0754*237041522*X*004010X092A1~ST*271*235181281~BHT*0022*11*000011629*20110120*0654~HL*1**20*1~NM1*PR*2*MEDICARE*****PI*MEDX~HL*2*1*21*1~NM1*1P*2******XX*1437104411~HL*3*2*22*0~TRN*1*1176270050250120110654280*9MEDIFAX  ~NM1*IL*1*ALBORN*JACK*B***MI*098183609A~REF*EJ*11629~N3*157 BUCKEYE TER~N4*HAINES CITY*FL*338448906~DMG*D8*19251107*M~DTP*307*RD8*20100101-20110101~DTP*442*D8*20101202~EB*1*IND**MA~DTP*307*D8*19901101~EB*1*IND**MB~DTP*307*D8*19901101~EB*C**96*MB**29*0~DTP*292*RD8*20100101-20101231~EB*F*IND*AD*MB**29*1860~DTP*292*RD8*20100101-20101231~EB*F*IND**MB*PHYSICAL & SPEECH THERAPY*29*1860~DTP*292*RD8*20100101-20101231~EB*X**45*MA**26~DTP*292*D8*20101130~LS*2120~NM1*1P*2******XX*1235283151~LE*2120~EB*C*IND*10***29***DB*3~DTP*292*RD8*20100101-20101231~SE*33*235181281~GE*1*237041522~IEA*1*237042407~";
		EdiReader reader = EdiReader.Factory.create();
		ByteArrayInputStream is = new ByteArrayInputStream(doc.getBytes());
		DOMResult output = new DOMResult();
		transformer.transform(new SAXSource(reader, new InputSource(is)), output);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//		transformer.transform(new DOMSource(output.getNode()), new StreamResult(System.out));
		XPathExpression testNoRemainingHlSegments =
			xpath.compile("/edi:edi//edi:segment[@type = 'HL']");
		NodeList nl = (NodeList) testNoRemainingHlSegments.evaluate(output.getNode(),
				XPathConstants.NODESET);
		assertEquals(0, nl.getLength());
		XPathExpression testHlBlockSegments = xpath.compile("/edi:edi//edi:block");
		nl = (NodeList) testHlBlockSegments.evaluate(output.getNode(), XPathConstants.NODESET);
		assertEquals(3, nl.getLength());
	}
}
