package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.ProcessContext;

public class SendTextStreamAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String text = "abc";
    String fileName = "file.txt";
    context.sendTextStreamResponse(text, fileName);
  }

}
