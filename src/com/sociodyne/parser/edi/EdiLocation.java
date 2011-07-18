package com.sociodyne.parser.edi;

public class EdiLocation extends ImmutableEdiLocation {

	public EdiLocation(String segment, int element) {
		super(segment, element);
	}

	public EdiLocation(String segment, int element, int subElement) {
		super(segment, element, subElement);
	}

	public void startSegment(String segment) {
		setSegment(segment);
		setElement(0);
		clearSubElement();
	}

	public void endSegment() {
		clearSubElement();
	}

	public void startElement() {
		nextElement();
	}

	public void endElement() {
		clearSubElement();
	}

	public void startSubElement() {
		if (hasSubElement()) {
			nextSubElement();
		} else {
			setSubElement(0);
		}
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public void setElement(int element) {
		this.element = element;
	}

	public void nextElement() {
		element++;
	}

	public void setSubElement(int subElement) {
		this.subElement = subElement;
	}

	public boolean hasSubElement() {
		return subElement != null;
	}

	public void nextSubElement() {
		if (subElement == null) {
			setSubElement(0);
		}
		subElement++;
	}

	public void clearSubElement() {
		this.subElement = null;
	}

	public static EdiLocation of(String segment, int element) {
		return new EdiLocation(segment, element);
	}

	public static EdiLocation of(String segment, int element, int subElement) {
		return new EdiLocation(segment, element, subElement);
	}

	public static EdiLocation copyOf(ImmutableEdiLocation location) {
		if (location.subElement == null) {
			return new EdiLocation(location.segment, location.element);
		} else {
			return new EdiLocation(location.segment, location.element, location.subElement);
		}
	}
}
