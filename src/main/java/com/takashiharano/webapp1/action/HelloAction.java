package com.takashiharano.webapp1.action;

import com.takashiharano.webapp1.ProcessContext;

public class HelloAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    context.sendTextResponse("Hello!");
  }

}
