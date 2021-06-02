package com.takashiharano.webapp1.action;

import com.takashiharano.webapp1.ProcessContext;

public class SendTextStreamAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String text = "abc";
    String fileName = "file.txt";
    context.sendTextStreamResponse(text, fileName);
  }

}
