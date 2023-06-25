/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;

public class PingAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    String etag = "\"" + System.currentTimeMillis() + "\"";
    context.setResponseHeader("ETag", etag);
    context.sendTextResponse("1");
  }

}
