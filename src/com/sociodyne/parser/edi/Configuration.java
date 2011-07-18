package com.sociodyne.parser.edi;

import com.sociodyne.LockingHolder;

public class Configuration {
	private char segmentTerminator = '~';
	private char elementSeparator = '*';
	private LockingHolder<Character> subElementSeparator = LockingHolder
			.of((Character)null);

	public static class Builder {
		private Character segmentTerminator;
		private Character elementSeparator;
		private Character subElementSeparator;

		public Builder setSegmentTerminator(char segmentTerminator) {
			this.segmentTerminator = segmentTerminator;
			return this;
		}

		public Builder setElementSeparator(char elementSeparator) {
			this.elementSeparator = elementSeparator;
			return this;
		}

		public Builder setSubElementSeparator(char subElementSeparator) {
			this.subElementSeparator = subElementSeparator;
			return this;
		}

		public Configuration build() {
			Configuration configuration = new Configuration();
			if (segmentTerminator != null) {
				configuration.segmentTerminator = segmentTerminator;
			}
			if (elementSeparator != null) {
				configuration.elementSeparator = elementSeparator;
			}
			if (subElementSeparator != null) {
				configuration.subElementSeparator.set(subElementSeparator);
			}

			return configuration;
		}
	}

	public boolean isSubElementSeparator(char ch) {
		return subElementSeparator.get() != null && subElementSeparator.get() == ch;
	}

	public void setSubElementSeparator(Character subElementSeparator) {
		// This is only allowed to change once during parsing: in the ISA
		// block.
		this.subElementSeparator.set(subElementSeparator);
		this.subElementSeparator.lock();
	}

	public boolean isSegmentTerminator(char ch) {
		return segmentTerminator == ch;
	}

	public boolean isElementSeparator(char ch) {
		return ch == elementSeparator;
	}
}