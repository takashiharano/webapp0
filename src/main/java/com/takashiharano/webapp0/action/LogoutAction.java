package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.session.SessionManager;

public class LogoutAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    AppManager appManager = AppManager.getInstance();
    SessionManager sessionManager = appManager.getSessionManager();
    sessionManager.logout(context);
    context.sendJsonResponse("OK", null);
  }

}
