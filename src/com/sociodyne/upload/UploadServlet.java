// Copyright 2011, Sociodyne LLC. All rights reserved.
// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Servlet that enables customers to upload files of various types and have them
 * processed.
 * The specific types are defined in {@link UploadModule}.
 * 
 * @author jkinner
 */
public class UploadServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(UploadServlet.class.getCanonicalName());
  private static final long serialVersionUID = -8614025407778899510L;
  private Map<String, UploadHandler> handlers;
  private MultipartChecker checker;
  private Provider<ServletFileUpload> uploadProvider;

  public static class MultipartChecker {

    public boolean isMultipartContent(HttpServletRequest request) {
      return ServletFileUpload.isMultipartContent(request);
    }
  }

  public static class ServletFileUploadProvider implements Provider<ServletFileUpload> {

    public ServletFileUpload get() {
      return new ServletFileUpload();
    }
  }

  public UploadServlet() {
  }

  @Inject
  public UploadServlet(MultipartChecker checker, Provider<ServletFileUpload> uploadProvider,
      Map<String, UploadHandler> handlers) {
    log.fine(handlers.size() + " handlers configured");
    if (log.isLoggable(Level.FINE)) {
      for (final Map.Entry<String, UploadHandler> handlersEntry : handlers.entrySet()) {
        log.fine("  " + handlersEntry.getKey() + " handled by " + handlersEntry.getValue());
      }
    }
    this.handlers = handlers;
    this.checker = checker;
    this.uploadProvider = uploadProvider;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (handlers == null || handlers.size() == 0) {
      throw new ServletException("No handlers configured");
    }

    log.fine("Handling content of type " + req.getContentType());

    if (checker.isMultipartContent(req)) {
      final ServletFileUpload fileUpload = uploadProvider.get();
      FileItemIterator files;
      final Map<String, Exception> errors = new HashMap<String, Exception>();

      try {
        files = fileUpload.getItemIterator(req);
      } catch (final FileUploadException e) {
        throw new ServletException("Could not start processing uploaded files", e);
      }

      try {
        while (files.hasNext()) {
          try {
            final FileItemStream fileStream = files.next();
            final String contentType = fileStream.getContentType();
            final UploadHandler handler = handlers.get(contentType);
            if (handler != null) {
              try {
                final InputStream inputStream = fileStream.openStream();
                handler.handle(inputStream);
              } catch (final Exception e) {
                errors.put(fileStream.getName(), e);
              }
            }
          } catch (final FileUploadException e) {
            throw new ServletException("Unable to process uploaded file", e);
          }
        }
      } catch (final FileUploadException e) {
        throw new ServletException("Unable to process uploaded files", e);
      }
    }

  }
}
