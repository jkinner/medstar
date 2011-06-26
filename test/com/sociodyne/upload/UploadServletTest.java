// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.easymock.EasyMock.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.doradosystems.upload.UploadHandler;
import com.doradosystems.upload.UploadServlet;
import com.google.inject.Provider;
import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

/**
 * 
 * Tests for the file-upload servlet
 * 
 * @author jkinner
 */
public class UploadServletTest extends MockTest {
	private static final String TEXT_PLAIN_MIME_TYPE = "text/plain";
	private @Mock(Mock.Type.STRICT) HttpServletRequest request;
	private @Mock(Mock.Type.STRICT) HttpServletResponse response;
	private @Mock(Mock.Type.STRICT) UploadServlet.MultipartChecker checker;
	private @Mock(Mock.Type.STRICT) Provider<ServletFileUpload> provider;
	private @Mock(Mock.Type.STRICT) ServletFileUpload upload;
	private @Mock(Mock.Type.STRICT) UploadHandler uploadHandler;

	@Override
	public void setUp() throws Exception {
		// Set up mocks
		super.setUp();
		expect(provider.get()).andStubReturn(upload);
	}

	public void testNullHandlers_throwsServletException() throws Exception {
		UploadServlet servlet = new UploadServlet(checker, provider, null);
		replay();
		try {
			servlet.doPost(request, response);
			fail("Expected ServletException");
		} catch (ServletException e) {
			// Expected
		}
	}

	@SuppressWarnings("unchecked")
	public void testEmptyHandlers_throwsServletException() throws Exception {
		UploadServlet servlet = new UploadServlet(checker, provider, Collections.EMPTY_MAP);
		replay();
		try {
			servlet.doPost(request, response);
			fail("Expected ServletException");
		} catch (ServletException e) {
			// Expected
		}
	}
	
	public void testOneHandler_handlerInvoked() throws Exception {
		expect(checker.isMultipartContent(request)).andReturn(true);

		UploadServlet servlet = new UploadServlet(checker, provider, Collections.singletonMap(TEXT_PLAIN_MIME_TYPE, uploadHandler));
		
		FileItemIterator itor = createMock(FileItemIterator.class);
		expect(upload.getItemIterator(same(request))).andReturn(itor);

		expectHandling(uploadHandler, upload, itor);

		expect(itor.hasNext()).andReturn(false);

		replay();

		servlet.doPost(request, response);
	}

	public void testOneHandlerMultipleFiles_handlerInvokedMultipleTimes() throws Exception {
		expect(checker.isMultipartContent(same(request))).andReturn(true);

		UploadServlet servlet = new UploadServlet(checker, provider, Collections.singletonMap(TEXT_PLAIN_MIME_TYPE, uploadHandler));
		
		FileItemIterator itor = createMock(FileItemIterator.class);
		expect(upload.getItemIterator(same(request))).andReturn(itor);

		expectHandling(uploadHandler, upload, itor);
		expectHandling(uploadHandler, upload, itor);

		expect(itor.hasNext()).andReturn(false);

		replay();

		servlet.doPost(request, response);
	}

	public void testOneHandlerDoesNotMatch_handlerNotInvoked() throws Exception {
		UploadServlet servlet = new UploadServlet(checker, provider, Collections.singletonMap(TEXT_PLAIN_MIME_TYPE, uploadHandler));
		expect(checker.isMultipartContent(same(request))).andReturn(true);
		
		FileItemIterator itor = createMock(FileItemIterator.class);
		expect(upload.getItemIterator(same(request))).andReturn(itor);

		FileItemStream stream = createMock(FileItemStream.class);
		expect(stream.getContentType()).andReturn(TEXT_PLAIN_MIME_TYPE + "kablam");

		expect(itor.hasNext()).andReturn(true);
		expect(itor.next()).andReturn(stream);

		expect(itor.hasNext()).andReturn(false);

		replay();

		servlet.doPost(request, response);
	}

	protected void expectHandling(UploadHandler mockHandler,
			ServletFileUpload upload, FileItemIterator itor)
			throws FileUploadException, IOException, Exception {
		FileItemStream stream = createMock(FileItemStream.class);
		expect(itor.hasNext()).andReturn(true);
		expect(itor.next()).andReturn(stream);

		expect(stream.getContentType()).andReturn(TEXT_PLAIN_MIME_TYPE);
		
		InputStream is = createMock(InputStream.class);
		expect(stream.openStream()).andReturn(is);
		
		// This is the crux of the test:
		mockHandler.handle(same(is));
	}
}
