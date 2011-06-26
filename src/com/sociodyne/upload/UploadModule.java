// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.upload;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.servlet.ServletModule;

/**
 * Guice module to bind the implementations of the handlers for the upload servlet.
 * 
 * @author jkinner
 */
public class UploadModule extends ServletModule {

	@Override
	protected void configureServlets() {
		// Upload servlet configuration
		bind(UploadServlet.class);

		// TODO(jkinner): Maybe make upload handlers hot-pluggable?
		// Bind implementations of file handlers
		MapBinder<String, UploadHandler> handlerBindings =
			MapBinder.newMapBinder(binder(), String.class, UploadHandler.class);
		handlerBindings.addBinding("text/csv").to(CsvUploadHandler.class).in(Singleton.class);
	}
}
