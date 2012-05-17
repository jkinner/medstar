// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.edi.parser;

import com.sociodyne.edi.Configuration;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class EdiXmlTransformer {
  private static final Transformer transformer;
  private static final EdiXmlReader reader = EdiXmlReader.Factory.create();

  static {
    final TransformerFactory tf = TransformerFactory.newInstance();
    try {
      transformer = tf.newTransformer();
    } catch (final TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private EdiXmlTransformer() {
    // Utility class
  }

  public static Document ediToXml(String edi) throws SAXException, TransformerException {
    DOMResult result = new DOMResult();
    try {
      transformer.transform(new SAXSource(reader, new InputSource(
          new ByteArrayInputStream(edi.getBytes("ASCII")))), result);
      return (Document)result.getNode();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String xmlToEdi(Document document, Configuration configuration)
      throws SAXException, TransformerException {
    final StringWriter stringWriter = new StringWriter();
    final XmlEdiWriter xmlEdiWriter = new XmlEdiWriter(new EdiWriter(configuration,
        stringWriter));
    final SAXResult output = new SAXResult(xmlEdiWriter);
    transformer.transform(new DOMSource(document), output);
    return stringWriter.toString();
  }
}
