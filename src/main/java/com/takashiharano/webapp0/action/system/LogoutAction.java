/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import com.libutil.StrUtil;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.session.SessionManager;

public class LogoutAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    String targetSid = context.getRequestParameter("sid");
    SessionManager sessionManager = context.getSessionManager();
    boolean success = true;
    if (StrUtil.isEmpty(targetSid)) {
      sessionManager.logout(context);
    } else {
      success = sessionManager.logout(targetSid);
    }
    String status = success ? "OK" : "NG";
    context.sendJsonResponse(status, null);
  }

}
