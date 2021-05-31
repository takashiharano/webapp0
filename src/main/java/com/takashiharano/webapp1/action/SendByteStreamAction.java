package com.takashiharano.webapp1.action;

import com.takashiharano.webapp1.ProcessContext;

public class SendByteStreamAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    byte[] b = { 0x41, 0x42, 0x43 };
    String fileName = "file.txt";
    context.sendByteStreamResponse(b, fileName);
  }

}
