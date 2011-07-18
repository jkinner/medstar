package com.sociodyne.parser.edi;

import com.sociodyne.parser.edi.Token.Type;

import com.google.common.base.Objects;

public class Token {
	public enum Type {
		WORD,
		ELEMENT_SEPARATOR,
		SUB_ELEMENT_SEPARATOR,
		SEGMENT_TERMINATOR
	}

	private Type type;
	private String value;

	public static final Token ELEMENT_SEPARATOR = new Token(Type.ELEMENT_SEPARATOR);
	public static final Token SUB_ELEMENT_SEPARATOR = new Token(Type.SUB_ELEMENT_SEPARATOR);
	public static final Token SEGMENT_TERMINATOR = new Token(Type.SEGMENT_TERMINATOR);

	public Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	public Token(Type type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Token) {
			Token that = (Token)o;
			return Objects.equal(that.type, type) && Objects.equal(that.value, value);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("type", type)
			.add("value", value)
			.toString();
	}

	public Type getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}

	public static Token word(String word) {
		return new Token(Token.Type.WORD, word);
	}
}
