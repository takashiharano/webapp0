/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;

public class ResetAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.isAdmin()) {
      context.sendJsonResponse("FORBIDDEN", null);
      return;
    }
    AppManager appManager = AppManager.getInstance();
    appManager.reset();
    String status = "OK";
    String message = null;
    if (!appManager.isReady()) {
      status = "ERROR";
      message = appManager.getErrorInfo();
    }
    context.sendJsonResponse(status, message);
  }

}
