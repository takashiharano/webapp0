/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action;

import com.libutil.zip.ZipUtil;
import com.takashiharano.webapp0.ProcessContext;

public class ZipAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String pathToZip = "C:/test/dir1";
    String fileName = "file.zip";
    byte[] b = ZipUtil.zip(pathToZip);
    context.sendByteStreamResponse(b, fileName);
  }

}
