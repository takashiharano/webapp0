package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.Log;

public class HelloAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String msg = "Hello!";
    Log.i(msg);
    context.sendJsonResponse("OK", msg, false);
  }

}
