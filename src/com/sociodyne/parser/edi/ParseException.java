package com.sociodyne.parser.edi;

public class ParseException extends Exception {
	protected ParseException() {
		// Only used by subclasses
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
}
