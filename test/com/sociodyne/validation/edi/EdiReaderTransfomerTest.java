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
		String doc="ISA*00*	*00*	*ZZ*CMS	*ZZ*T000000011*090326*1705*U*00401*002417703*0*P*|~GS*HB*CMS*T000000011*20090326*1705*2381770*X*004010X092A1~ST*271*2377396~BHT*0022*11*ALL*20090326*17055389~HL*1**20*1~NM1*PR*2*CMS*****PI*CMS~HL*2*1*21*1~NM1*1P*2*TEST*****XX*1234567893~HL*3*2*22*0~TRN*2*TEST-TEST*9TEST~NM1*IL*1*SMITH*MARY****MI*123456789A~N3*123MAINSTREET*~N4*ANYTOWN*MD*999999999~DMG*D8*19240901*F~INS*Y*18*001*25~DTP*307*RD8*20070101-20090325~EB*1*IND**MA~DTP*307*D8*19890801~EB*K**47*MA**33***LA*60~EB*C**47*MA**29*992~EB*F**47*MA**29***DY*60~EB*B**47*MA**29*248**DY*30~EB*F**AG*MA**29***DY*20~EB*B**AG*MA**29*124**DY*80~EB*1*IND**MB~DTP*307*D8*19890801~EB*C**96*MB**29*0~DTP*292*RD8*20090101-20091231~EB*C**96*MB**29*0~DTP*292*RD8*20080101-20081231~EB*C**96*MB**29*0~DTP*292*RD8*20070101-20071231~EB*D*IND**MB*********HC|G0389~DTP*348*D8*20070701~EB*D*IND**MB*********HC|77057~DTP*348*D8*20070101~EB*D*IND**MB*********HC|82270~DTP*348*D8*20070101~EB*D*IND**MB*********HC|Q0091~DTP*348*D8*20050701~EB*D*IND**MB*********HC|82951~DTP*348*D8*20050101~EB*D*IND**MB*********HC|84478~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82950~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82947~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82465~DTP*348*D8*20050101~EB*D*IND**MB*********HC|80061~DTP*348*D8*20050101~EB*D*IND**MB*********HC|83718~DTP*348*D8*20050101~EB*D*IND**MB*********HC|G0202~DTP*348*D8*20040201~EB*D*IND**MB*********HC|G0328~DTP*348*D8*20040101~EB*D*IND**MB*********HC|G0118~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0117~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0101~DTP*348*D8*20010701~EB*D*IND**MB*********HC|P3000~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0148~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0147~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0145~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0144~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0143~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0121~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0123~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0105~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0106~DTP*348*D8*19980101~EB*D*IND**MB*********HC|82270~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0104~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0120~DTP*348*D8*19980101~EB*D*IND**MB*********HC|90732~DTP*348*D8*19890801~EB*F*IND*67*MB**29***P6*8~EB*F*IND*AD*MB**29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AE*MB***29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AD*MB**29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AE*MB***29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AD*MB**29*1780~DTP*292*RD8*20070101-20071231~EB*F*IND*AE*MB***29*1780~DTP*292*RD8*20070101-20071231~EB*X**45*MA**26~DTP*292*RD8*20080208-20081217~LS*2120~NM1*1P*2******XX*1427038314~LE*2120~EB*X**45*MA**26~DTP*292*D8*20061016~LS*2120~NM1*1P*2******SV*031549~LE*2120~EB*C*IND*10***29***DB*3~DTP*292*RD8*20090101-20091231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20080101-20081231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20070101-20071231~EB*R**88*OT~REF*18*H9999999~DTP*292*D8*20070801~LS*2120~NM1*PR*2*ABCHEALTHPLAN~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**88*OT~REF*18*S9999999~DTP*292*RD8*20070101-20070731~LS*2120~NM1*PR*2*ABCINSURANCECOMPANY~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**30*HN~REF*18*H9999999~DTP*290*D8*20070801~LS*2120~NM1*PRP*2*ABCHEALTHPLAN~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R*IND*30*14~DTP*290*D8*19960912~LS*2120~NM1*PRP*2*SMITH~LE*2120~SE*156*2377396~GE*1*2381770~IEA*1*002417703~";
		EdiReader reader = EdiReader.Factory.create();
		ByteArrayInputStream is = new ByteArrayInputStream(doc.getBytes());
		DOMResult output = new DOMResult();
		transformer.transform(new SAXSource(reader, new InputSource(is)), output);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(new DOMSource(output.getNode()), new StreamResult(System.out));
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
