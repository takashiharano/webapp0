/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.sample;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.util.Log;

public class HelloAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String s = context.getRequestParameter("text");
    String msg = "Hello! " + s;
    Log.i(msg);
    context.sendJsonResponse("OK", msg, false);
  }

}
