package com.sociodyne.parser.edi;

/**
 * Captures the sub-element separator from the ISA segment.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
public class IsaEdiHandler implements EdiHandler {
	private static final int ISA_SUBELEMENT_SEPARATOR_ELEMENT = 16;

	private final Configuration configuration;
	private final EdiHandler delegate;
	int currentElement = 0;

	public IsaEdiHandler(Configuration configuration, EdiHandler delegate) {
		this.configuration = configuration;
		this.delegate = delegate;
	}

	public void startSegment(String segmentIdentifier) throws ParseException {
		delegate.startSegment(segmentIdentifier);
	}

	public void endSegment() throws ParseException {
		delegate.endSegment();
	}

	public void startElement(String contents) throws ParseException {
		currentElement++;
		if (currentElement == ISA_SUBELEMENT_SEPARATOR_ELEMENT) {
			configuration.setSubElementSeparator(contents.charAt(0));
		}
		delegate.startElement(contents);
	}

	public void endElement() throws ParseException {
		delegate.endElement();
	}

	public void subElement(String contents) throws ParseException {
		delegate.subElement(contents);
	}

	public void startLoop(String segmentIdentifier) throws ParseException {
		delegate.startLoop(segmentIdentifier);
	}

	public void endLoop() throws ParseException {
		delegate.endLoop();
	}
}
