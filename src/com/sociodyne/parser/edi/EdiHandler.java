package com.sociodyne.parser.edi;

public interface EdiHandler {
	void startSegment(String segmentIdentifier) throws ParseException;

	void endSegment() throws ParseException;

	void startElement(String contents) throws ParseException;
	
	void endElement() throws ParseException;

	void subElement(String contents) throws ParseException;

	void startLoop(String segmentIdentifier) throws ParseException;

	void endLoop() throws ParseException;
}
