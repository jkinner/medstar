package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.IOException;
import java.io.Reader;

import javax.annotation.Nullable;

import org.xml.sax.SAXException;

abstract class Parser {
	protected StringBuffer accumulator = new StringBuffer();
	protected Configuration configuration;
	protected Location location;
	protected Reader reader;

	protected Parser(Reader reader, Configuration configuration, Location location) {
		this.reader = reader;
		this.configuration = configuration;
		this.location = location;
	}

	public Integer parse()
			throws IOException, SAXException {
		int read;
		Character terminalToken = null;
		while ((read = reader.read()) != -1) {
			char ch = (char) read;

			if (ch == '\n') {
				location.nextLine();
			} else {
				location.nextChar();
			}

			// Will return the character that caused the current parser to terminate, or null
			// if the parser should continue.
			terminalToken = handleCharacter(ch);
			if (terminalToken != null) {
				handleTerminalToken(terminalToken);
				return (int)terminalToken;
			}
		}

		handleTerminalToken(terminalToken);

		return terminalToken != null?(int)terminalToken:-1;
	}

	protected void blank(StringBuffer accumulator) {
		accumulator.delete(0, accumulator.length());
	}

	protected abstract Character handleCharacter(char ch) throws IOException, SAXException;
	
	/**
	 * Handles the terminal token for this parsing phase.
	 * 
	 * @param ch the character read, or {@code null} if EOF encountered
	 */
	protected abstract void handleTerminalToken(@Nullable Character ch)
		throws IOException, SAXException;
}