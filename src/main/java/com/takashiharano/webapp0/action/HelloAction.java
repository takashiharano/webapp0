package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.ProcessContext;

public class HelloAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    context.sendTextResponse("Hello!");
  }

}
