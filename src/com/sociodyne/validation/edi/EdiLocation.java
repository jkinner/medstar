package com.sociodyne.validation.edi;

public class EdiLocation extends ImmutableEdiLocation {

	public EdiLocation(String segment, int element) {
		super(segment, element);
	}

	public EdiLocation(String segment, int element, int subElement) {
		super(segment, element, subElement);
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

}
