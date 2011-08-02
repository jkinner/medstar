package com.sociodyne.edi.builder;

/**
 * Types of EDI documents supported by the system. These types are passed to the {@link EdiBuilder}
 * to define which document type the envelope should indicate is in the payload.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public enum DocumentType {
  VALIDATION_REQUEST("270"),
  VALIDATION_RESPONSE("271");

  private final String documentIdentifierCode;

  private DocumentType(String documentIdentifierCode) {
    this.documentIdentifierCode = documentIdentifierCode;
  }
  
  public String getDocumentIdentifierCode() {
    return documentIdentifierCode;
  }
}