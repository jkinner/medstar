package com.sociodyne.validation.edi;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Identifies the location of a field in an EDI document. Element and sub-elements are indexed
 * by 1 in order to match specifications.
 * 
 * @author jkinner@sociodyne.com (Jason Kinenr)
 */
public class ImmutableEdiElementIdentifier {
	protected String segment;
	protected int element;
	@Nullable protected Integer subElement;

	public ImmutableEdiElementIdentifier(String segment, int element) {
		this.segment = segment;
		this.element = element;
		subElement = null;
	}

	public ImmutableEdiElementIdentifier(String segment, int element, int subElement) {
		this.segment = segment;
		this.element = element;
		this.subElement = subElement;
	}

	public String getSegment() {
		return segment;
	}

	public int getIndex() {
		return element;
	}

	public int getSubElement() {
		return subElement == null?-1:subElement;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(segment, element);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) { return true; }
		
		if (o instanceof ImmutableEdiElementIdentifier) {
			ImmutableEdiElementIdentifier that = (ImmutableEdiElementIdentifier)o;
			return Objects.equal(segment, that.segment) && Objects.equal(element, that.element)
				&& Objects.equal(subElement, that.subElement);
		}

		return false;
	}

	public static ImmutableEdiElementIdentifier of(String segment, int element) {
		return new ImmutableEdiElementIdentifier(segment, element);
	}

	public static ImmutableEdiElementIdentifier of(String segment, int element, int subElement) {
		return new ImmutableEdiElementIdentifier(segment, element, subElement);
	}
}
