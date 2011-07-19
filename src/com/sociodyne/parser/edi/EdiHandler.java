package com.sociodyne.parser.edi;

public interface EdiHandler {
	void startSegment(String segmentIdentifier) throws EdiException;

	void endSegment() throws EdiException;

	void startElement(String contents) throws EdiException;
	
	void endElement() throws EdiException;

	void subElement(String contents) throws EdiException;

	void startLoop(String segmentIdentifier) throws EdiException;

	void endLoop() throws EdiException;
}
