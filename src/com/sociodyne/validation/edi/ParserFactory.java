package com.sociodyne.validation.edi;

import com.sociodyne.validation.edi.EdiReader.Configuration;
import com.sociodyne.validation.edi.EdiReader.Location;

import java.io.Reader;

import org.xml.sax.ContentHandler;

/**
 * 
 * @author jkinner
 */
interface ParserFactory<P extends Parser> {
	P create(Reader reader, Configuration configuration,
		ContentHandler contentHandler, Location location);
}
