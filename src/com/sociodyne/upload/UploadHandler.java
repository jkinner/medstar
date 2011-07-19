// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.upload;

import java.io.InputStream;

public interface UploadHandler {

  void handle(InputStream fileStream) throws Exception;
}
