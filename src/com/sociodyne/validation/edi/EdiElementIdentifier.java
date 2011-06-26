package com.sociodyne.validation.edi;

public class EdiElementIdentifier extends ImmutableEdiElementIdentifier {

	public EdiElementIdentifier(String segment, int element) {
		super(segment, element);
	}

	public EdiElementIdentifier(String segment, int element, int subElement) {
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

	public static EdiElementIdentifier of(String segment, int element) {
		return new EdiElementIdentifier(segment, element);
	}

	public static EdiElementIdentifier of(String segment, int element, int subElement) {
		return new EdiElementIdentifier(segment, element, subElement);
	}

}
