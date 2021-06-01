package com.takashiharano.webapp1.action;

import com.takashiharano.webapp1.ProcessContext;

public class SendFileAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String path = "C:/test/a.txt";
    String fileName = "file.txt";
    context.sendFileResponse(path, fileName);
  }

}
