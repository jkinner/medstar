package com.sociodyne.edi.parser;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import com.google.common.collect.Iterators;

final class EdiNamespaceContext implements NamespaceContext {

  public String getNamespaceURI(String prefix) {
    return (prefix.equals("") || prefix.equals("edi")) ? EdiXmlAdapter.NAMESPACE_URI : null;
  }

  public String getPrefix(String namespaceUri) {
    return namespaceUri.equals(EdiXmlAdapter.NAMESPACE_URI) ? "" : null;
  }

  public Iterator<String> getPrefixes(String namespaceUri) {
    return namespaceUri.equals(EdiXmlAdapter.NAMESPACE_URI) ? Iterators.forArray(new String[] { "",
        "edi" }) : Iterators.<String> emptyIterator();
  }
}
