/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.sample;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;

public class SendByteStreamAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    byte[] b = { 0x41, 0x42, 0x43 };
    String fileName = "file.txt";
    context.sendByteStreamResponse(b, fileName);
  }

}
