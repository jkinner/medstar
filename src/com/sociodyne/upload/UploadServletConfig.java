// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.upload;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Guice injector factory for the upload servlet.
 * 
 * @author jkinner
 */
public class UploadServletConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new UploadModule());
	}

}
