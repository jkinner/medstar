package com.sociodyne.edi;

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