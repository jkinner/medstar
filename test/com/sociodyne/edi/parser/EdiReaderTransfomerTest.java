// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

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

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EdiReaderTransfomerTest extends TestCase {

  private static final String VALID_ISA_HEADER = "ISA*00*        *00*        *ZZ*T000000011  *ZZ*CMS *"
      + "050516*0734*U*00401*000005014*1*P*:~";

  private static final Transformer transformer;
  private static final XPath xpath;
  private static final NamespaceContext namespaceContext;

  static {
    final TransformerFactory tf = TransformerFactory.newInstance();
    namespaceContext = new EdiNamespaceContext();
    try {
      transformer = tf.newTransformer();
      final XPathFactory f = XPathFactory.newInstance();
      xpath = f.newXPath();
      xpath.setNamespaceContext(namespaceContext);
    } catch (final TransformerConfigurationException e) {
      System.err.println("Unable to create JAXP Transformer");
      throw new RuntimeException(e);
    }
  }

  public void testParseSegment_withElements_succeeds() throws Exception {
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(VALID_ISA_HEADER.getBytes());
    final DOMResult output = new DOMResult();
    transformer.transform(new SAXSource(reader, new InputSource(is)), output);

    final Node root = output.getNode();
    final Document doc = (Document) root;

    final XPathExpression testSegmentType = xpath.compile("/edi:edi/edi:segment[@type = 'ISA']");
    Node n = (Node) testSegmentType.evaluate(doc, XPathConstants.NODE);
    assertNotNull("Expected an ISA segment", n);
    final XPathExpression testCMS = xpath
        .compile("/edi:edi/edi:segment[@type = 'ISA']/edi:element[8]");
    n = (Node) testCMS.evaluate(output.getNode(), XPathConstants.NODE);
    assertNotNull("Expected an ISA element in ISA08", n);
    assertEquals("CMS ", n.getTextContent());
  }

  private void dumpOutput(DOMResult output) throws TransformerException {
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new DOMSource(output.getNode()), new StreamResult(System.out));
  }

  public void testParseSegment_noElements_throwsParseException() throws Exception {
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(("ISA~").getBytes());
    final DOMResult output = new DOMResult();
    try {
      transformer.transform(new SAXSource(reader, new InputSource(is)), output);
      fail("Expected TransformerException");
    } catch (final TransformerException e) {
      final SAXException eCause = (SAXException) e.getCause();
      assertEquals(UnexpectedTokenException.class, eCause.getCause().getClass());
    }
  }

  public void testParseSegment_noTerminator_throwsEOFException() throws Exception {
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(("ISA").getBytes());
    final DOMResult output = new DOMResult();
    try {
      transformer.transform(new SAXSource(reader, new InputSource(is)), output);
      fail("Expected EOFException");
    } catch (final TransformerException e) {
      assertEquals(EOFException.class, e.getCause().getClass());
    }
  }

  public void testParseSegment_withElementsNoTerminator_throwsEOFException() throws Exception {
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(("ISA*00*ZZ*").getBytes());
    final DOMResult output = new DOMResult();
    try {
      transformer.transform(new SAXSource(reader, new InputSource(is)), output);
      fail("Expected EOFException");
    } catch (final TransformerException e) {
      assertEquals(EOFException.class, e.getCause().getClass());
    }
  }

  public void testParseComplexDocument_succeeds() throws Exception {
    final String doc = "ISA*00*          *00*          *ZZ*TPG00094935    *ZZ*TPG00094935    *100413*0219*U*00401*240140919*0*P*:~GS*HB*OXFRD*LLX1210001*20100413*0219*1*X*004010X092A1~ST*271*000002624~BHT*0022*11*000002624*20100413*0219~HL*1**20*1~NM1*PR*2*OXFORD HEALTH PLANS*****PI*00016~HL*2*1*21*1~NM1*1P*2******XX*1770538696~HL*3*2*22*0~TRN*1*1162550050250413100218369*9MEDIFAX  ~NM1*IL*1*WEISSENBERGER*ERICH*G***MI*66572501~REF*EJ*8906~N3*2 WESTWOOD DR~N4*HADDONFIELD*NJ*08033*US~DMG*D8*19460217*M~EB*1*IND*30*C1*FREEDOM, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*19950415~EB*B*IND*49*C1*FREEDOM, FREEDOM**100*****Y~DTP*307*D8*20100413~EB*A*IND*49*****.3****N~DTP*307*D8*20100413~EB*C*IND*49****750*****N~DTP*307*D8*20100413~EB*B*IND*51*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*FREEDOM, FREEDOM*27*20*****Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*HIGGINS*ALEXANDER****PI*JP111~PER*IC**TE*8565476000~LE*2120~EB*B*IND*98***27*20*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*A*IND*98***27**.3****N~DTP*307*D8*20100413~EB*C*IND*98***27*750*****N~DTP*307*D8*20100413~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*4*2*22*0~TRN*1*1104460050230413100218399*9MEDIFAX  ~NM1*IL*1*DAS*NEHA****MI*37968103~REF*EJ*8873~DMG*D8*20030519*F~INS*Y*18*001*25~EB*1*IND*30*C1*FREEDOM, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*20030519~EB*B*IND*49*C1*FREEDOM, FREEDOM**0*****Y~DTP*307*D8*20100413~EB*A*IND*49*****.2****N~DTP*307*D8*20100413~EB*C*IND*49****400*****N~DTP*307*D8*20100413~EB*B*IND*51*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*FREEDOM, FREEDOM*27*15*****Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*THOMAS*SUSHEELA****PI*P465438~PER*IC**TE*9735840002~LE*2120~EB*B*IND*98***27*15*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*A*IND*98***27**.2****N~DTP*307*D8*20100413~EB*C*IND*98***27*400*****N~DTP*307*D8*20100413~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*5**20*1~NM1*PR*2*Oxford Health Plans*****PI*OXFRD~HL*6*5*21*1~NM1*IP*2******XX*1770538696~HL*7*6*22*1~TRN*1*1157110050240413100218449*9MEDIFAX  ~NM1*1L*1******MI*71204603~AAA*Y**67*C~HL*8*7*23*0~NM1*03*1*DAUBERT*JEREMY A~REF*EJ*8795~DMG*D8*19990222*M~DTP*307*D8*20100413~EB*V**30~MSG*50-RH0247 - PATIENT NOT FOUND~HL*9**20*1~NM1*PR*2*OXFORD HEALTH PLANS*****PI*00016~HL*10*9*21*1~NM1*1P*2******XX*1770538696~HL*11*10*22*0~TRN*1*1104730050230413100218476*9MEDIFAX  ~NM1*IL*1*CERNUTO*RYAN****MI*30859104~REF*EJ*8868~DMG*D8*20010918*M~INS*Y*18*001*25~EB*6**30~DTP*307*D8*20100413~EB*6*IND*49~DTP*307*D8*20100413~EB*6*IND*51~DTP*307*D8*20100413~EB*6*IND*52~DTP*307*D8*20100413~EB*6*IND*98***27~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*20010918~EB*CB*IND*60~DTP*357*D8*20100131~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*12*10*22*0~TRN*1*1157490050240413100218514*9MEDIFAX  ~NM1*IL*1*MOHANTY*NIBEDITA****MI*37968102~REF*EJ*8887~DMG*D8*19681217*F~EB*1*IND*30*C1*FREEDOM, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*19900901~EB*B*IND*49*C1*FREEDOM, FREEDOM**0*****Y~DTP*307*D8*20100413~EB*A*IND*49*****.2****N~DTP*307*D8*20100413~EB*C*IND*49****400*****N~DTP*307*D8*20100413~EB*B*IND*51*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*FREEDOM, FREEDOM*27*50*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*FREEDOM, FREEDOM*27*15*****Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*PETERS*KAREN****XX*1609822501~PER*IC**TE*9733473277~LE*2120~EB*B*IND*98***27*15*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*A*IND*98***27**.2****N~DTP*307*D8*20100413~EB*C*IND*98***27*400*****N~DTP*307*D8*20100413~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*13*10*22*0~TRN*1*1157590050240413100218547*9MEDIFAX  ~NM1*IL*1*CASTIGLIONE*JESSICA****MI*15486105~REF*EJ*8918~DMG*D8*19970528*F~INS*Y*18*001*25~EB*1*IND*30*C1*FREEDOM, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*19970528~EB*B*IND*49*C1*FREEDOM, FREEDOM**0*****Y~DTP*307*D8*20100413~EB*A*IND*49*****.2****N~DTP*307*D8*20100413~EB*C*IND*49****250*****N~DTP*307*D8*20100413~EB*B*IND*51*C1*FREEDOM, FREEDOM*27*35*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*FREEDOM, FREEDOM*27*35*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*FREEDOM, FREEDOM*27*10*****Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*HAMMOND*BETTY****XX*1952345753~PER*IC**TE*7322541515~LE*2120~EB*B*IND*98***27*10*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*A*IND*98***27**.2****N~DTP*307*D8*20100413~EB*C*IND*98***27*250*****N~DTP*307*D8*20100413~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*14*10*22*0~TRN*1*1123870050220413100218585*9MEDIFAX  ~NM1*IL*1*FRIEDMAN*CHAYA****MI*42481708~REF*EJ*8950~DMG*D8*20051115*F~INS*Y*18*001*25~EB*1*IND*30*C1*FREEDOM, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*20051115~EB*B*IND*49*C1*FREEDOM, FREEDOM**500*****Y~DTP*307*D8*20100413~EB*A*IND*49*****.3****N~DTP*307*D8*20100413~EB*C*IND*49****3000*****N~DTP*307*D8*20100413~EB*B*IND*51*C1*FREEDOM, FREEDOM*27*150*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*FREEDOM, FREEDOM*27*150*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*FREEDOM, FREEDOM*27*30****N*Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*SHANIK*ROBERT****XX*1306864962~PER*IC**TE*7323410720~LE*2120~EB*B*IND*98***27*50*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*A*IND*98***27**.3****N~DTP*307*D8*20100413~EB*C*IND*98***27*3000*****N~DTP*307*D8*20100413~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*15*10*22*0~TRN*1*1163440050250413100219021*9MEDIFAX  ~NM1*IL*1*VAZQUEZ*JAMESON*A***MI*106147805~REF*EJ*8916~N3*119 EATON AVE~N4*TRENTON*NJ*08619*USA~DMG*D8*20040421*M~INS*Y*18*001*25~EB*1*IND*30*C1*HMO, FREEDOM*******Y~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*20040421~EB*B*IND*49*C1*HMO, FREEDOM**500*****Y~DTP*307*D8*20100413~EB*B*IND*51*C1*HMO, FREEDOM*27*150*****Y~DTP*307*D8*20100413~EB*B*IND*52*C1*HMO, FREEDOM*27*150*****Y~DTP*307*D8*20100413~EB*B*IND*98*C1*HMO, FREEDOM*27*15****N*Y~DTP*307*D8*20100413~LS*2120~NM1*P3*1*SALTSTEIN*ELLIOTT****PI*MEP115~PER*IC**TE*6095815100~LE*2120~EB*B*IND*98***27*50*****Y~DTP*307*D8*20100413~LS*2120~NM1*73*2~PER*IC*NO CONTACT INFO~LE*2120~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*16*10*22*0~TRN*1*1163620050250413100219064*9MEDIFAX  ~NM1*IL*1*CERNUTO*HAILEY BRIELLE****MI*30859103~REF*EJ*8867~DMG*D8*19991218*F~INS*Y*18*001*25~EB*6**30~DTP*307*D8*20100413~EB*6*IND*49~DTP*307*D8*20100413~EB*6*IND*51~DTP*307*D8*20100413~EB*6*IND*52~DTP*307*D8*20100413~EB*6*IND*98***27~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*19991218~EB*CB*IND*60~DTP*357*D8*20100131~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~HL*17*10*22*0~TRN*1*1158100050240413100219090*9MEDIFAX  ~NM1*IL*1*MCDUFFIE*XAVIER*C***MI*35299104~REF*EJ*8797~DMG*D8*19970626*M~INS*Y*18*001*25~EB*6**30~DTP*307*D8*20100413~EB*6*IND*49~DTP*307*D8*20100413~EB*6*IND*51~DTP*307*D8*20100413~EB*6*IND*52~DTP*307*D8*20100413~EB*6*IND*98***27~DTP*307*D8*20100413~EB*CB*IND*60~DTP*356*D8*19970626~EB*CB*IND*60~DTP*357*D8*20090630~EB*P**1~MSG*INFORMATION PROVIDED HEREIN IS NOT A GUARANTEE OF PAYMENT OR OF COVERAGE. BENEFIT DETERMINATIONS DEPEND ON A NUMBER OF FACTORS, INCLUDING MEDICAL NECESSITY. OXFORD EXPRESSLY RESERVES THE RIGHT TO CHANGE ANY INFORMATION PROVIDED.~SE*314*000002624~GE*1*1~IEA*1*240140919~";
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(doc.getBytes());
    final DOMResult output = new DOMResult();
    transformer.transform(new SAXSource(reader, new InputSource(is)), output);
    final XPathExpression testAllHlSegments = xpath.compile("/edi:edi//edi:loop[@type = 'HL']");
    final NodeList nl = (NodeList) testAllHlSegments.evaluate(output.getNode(),
        XPathConstants.NODESET);
    assertEquals(17, nl.getLength());
  }

  public void testParseSampleDocument_succeeds() throws Exception {
    final String doc = "ISA*00*	*00*	*ZZ*CMS	*ZZ*T000000011*090326*1705*U*00401*002417703*0*P*|~GS*HB*CMS*T000000011*20090326*1705*2381770*X*004010X092A1~ST*271*2377396~BHT*0022*11*ALL*20090326*17055389~HL*1**20*1~NM1*PR*2*CMS*****PI*CMS~HL*2*1*21*1~NM1*1P*2*TEST*****XX*1234567893~HL*3*2*22*0~TRN*2*TEST-TEST*9TEST~NM1*IL*1*SMITH*MARY****MI*123456789A~N3*123MAINSTREET*~N4*ANYTOWN*MD*999999999~DMG*D8*19240901*F~INS*Y*18*001*25~DTP*307*RD8*20070101-20090325~EB*1*IND**MA~DTP*307*D8*19890801~EB*K**47*MA**33***LA*60~EB*C**47*MA**29*992~EB*F**47*MA**29***DY*60~EB*B**47*MA**29*248**DY*30~EB*F**AG*MA**29***DY*20~EB*B**AG*MA**29*124**DY*80~EB*1*IND**MB~DTP*307*D8*19890801~EB*C**96*MB**29*0~DTP*292*RD8*20090101-20091231~EB*C**96*MB**29*0~DTP*292*RD8*20080101-20081231~EB*C**96*MB**29*0~DTP*292*RD8*20070101-20071231~EB*D*IND**MB*********HC|G0389~DTP*348*D8*20070701~EB*D*IND**MB*********HC|77057~DTP*348*D8*20070101~EB*D*IND**MB*********HC|82270~DTP*348*D8*20070101~EB*D*IND**MB*********HC|Q0091~DTP*348*D8*20050701~EB*D*IND**MB*********HC|82951~DTP*348*D8*20050101~EB*D*IND**MB*********HC|84478~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82950~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82947~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82465~DTP*348*D8*20050101~EB*D*IND**MB*********HC|80061~DTP*348*D8*20050101~EB*D*IND**MB*********HC|83718~DTP*348*D8*20050101~EB*D*IND**MB*********HC|G0202~DTP*348*D8*20040201~EB*D*IND**MB*********HC|G0328~DTP*348*D8*20040101~EB*D*IND**MB*********HC|G0118~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0117~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0101~DTP*348*D8*20010701~EB*D*IND**MB*********HC|P3000~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0148~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0147~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0145~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0144~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0143~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0121~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0123~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0105~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0106~DTP*348*D8*19980101~EB*D*IND**MB*********HC|82270~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0104~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0120~DTP*348*D8*19980101~EB*D*IND**MB*********HC|90732~DTP*348*D8*19890801~EB*F*IND*67*MB**29***P6*8~EB*F*IND*AD*MB**29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AE*MB***29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AD*MB**29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AE*MB***29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AD*MB**29*1780~DTP*292*RD8*20070101-20071231~EB*F*IND*AE*MB***29*1780~DTP*292*RD8*20070101-20071231~EB*X**45*MA**26~DTP*292*RD8*20080208-20081217~LS*2120~NM1*1P*2******XX*1427038314~LE*2120~EB*X**45*MA**26~DTP*292*D8*20061016~LS*2120~NM1*1P*2******SV*031549~LE*2120~EB*C*IND*10***29***DB*3~DTP*292*RD8*20090101-20091231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20080101-20081231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20070101-20071231~EB*R**88*OT~REF*18*H9999999~DTP*292*D8*20070801~LS*2120~NM1*PR*2*ABCHEALTHPLAN~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**88*OT~REF*18*S9999999~DTP*292*RD8*20070101-20070731~LS*2120~NM1*PR*2*ABCINSURANCECOMPANY~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**30*HN~REF*18*H9999999~DTP*290*D8*20070801~LS*2120~NM1*PRP*2*ABCHEALTHPLAN~N3*123MAINSTREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R*IND*30*14~DTP*290*D8*19960912~LS*2120~NM1*PRP*2*SMITH~LE*2120~SE*156*2377396~GE*1*2381770~IEA*1*002417703~";
    final EdiXmlReader reader = EdiXmlReader.Factory.create();
    final ByteArrayInputStream is = new ByteArrayInputStream(doc.getBytes());
    final DOMResult output = new DOMResult();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new SAXSource(reader, new InputSource(is)), output);
    final XPathExpression testAllHlSegments = xpath.compile("/edi:edi//edi:segment[@type = 'HL']");
    final NodeList nl = (NodeList) testAllHlSegments.evaluate(output.getNode(),
        XPathConstants.NODESET);
    assertEquals(3, nl.getLength());
  }
}
