package com.sociodyne.parser.edi;

import com.sociodyne.parser.Location;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Identifies the location of a field in an EDI document. Element and sub-elements are indexed
 * by 1 in order to match specifications.
 * 
 * @author jkinner@sociodyne.com (Jason Kinenr)
 */
public class ImmutableEdiLocation extends Location {
	protected String segment;
	protected int element;
	@Nullable protected Integer subElement;

	public ImmutableEdiLocation(String segment, int element) {
		this.segment = segment;
		this.element = element;
		subElement = null;
	}

	public ImmutableEdiLocation(String segment, int element, int subElement) {
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
		
		if (o instanceof ImmutableEdiLocation) {
			ImmutableEdiLocation that = (ImmutableEdiLocation)o;
			return Objects.equal(segment, that.segment) && Objects.equal(element, that.element)
				&& Objects.equal(subElement, that.subElement);
		}

		return false;
	}

	public static ImmutableEdiLocation of(String segment, int element) {
		return new ImmutableEdiLocation(segment, element);
	}

	public static ImmutableEdiLocation of(String segment, int element, int subElement) {
		return new ImmutableEdiLocation(segment, element, subElement);
	}

	public static ImmutableEdiLocation copyOf(ImmutableEdiLocation location) {
		return new ImmutableEdiLocation(location.segment, location.element, location.subElement);
	}

	@Override
	public String toString() {
		StringBuffer locationBuffer = new StringBuffer(super.toString());
		locationBuffer.append(", segment ").append(segment);
		if (element > 0) {
			locationBuffer.append(", element ").append(element);
		}
		if (subElement != null) {
			locationBuffer.append("subelement ").append(subElement);
		}

		return locationBuffer.toString();
	}
}
