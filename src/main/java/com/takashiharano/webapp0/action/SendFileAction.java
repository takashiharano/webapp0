package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.ProcessContext;

public class SendFileAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String path = "C:/test/a.txt";
    String fileName = "file.txt";
    context.sendFileResponse(path, fileName);
  }

}
