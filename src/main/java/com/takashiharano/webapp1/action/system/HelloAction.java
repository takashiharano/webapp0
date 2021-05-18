package com.takashiharano.webapp1.action.system;

import com.takashiharano.webapp1.ProcessContext;
import com.takashiharano.webapp1.action.Action;

public class HelloAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    context.sendTextResponse("Hello!");
  }

}
