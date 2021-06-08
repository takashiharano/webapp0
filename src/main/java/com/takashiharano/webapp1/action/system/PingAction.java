package com.takashiharano.webapp1.action.system;

import com.takashiharano.webapp1.ProcessContext;
import com.takashiharano.webapp1.action.Action;

public class PingAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String etag = "\"" + System.currentTimeMillis() + "\"";
    context.setResponseHeader("ETag", etag);
    context.sendTextResponse("1");
  }

}
