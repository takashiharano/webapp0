package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class LoginAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    AppManager appManager = AppManager.getInstance();
    SessionManager sessionManager = appManager.getSessionManager();

    sessionManager.cleanInvalidatedSessionInfo();

    String username = context.getRequestParameter("id");
    String pass = context.getBSB64DecodedRequestParameter("pw");

    UserManager userManager = UserManager.getInstance();
    String result = userManager.authenticate(username, pass);
    String status = "NG";

    if ("OK".equals(result)) {
      status = "OK";
      Log.i("Login : " + username);
      status = "OK";
      sessionManager.onLoggedIn(context, username);
    } else if ("PASSWORD_MISMATCH".equals(result)) {
      Log.w("Login Error: user=" + username);
    } else if ("NO_SUCH_USER".equals(result)) {
      Log.w("Login Error: NO_SUCH_USER");
    } else {
      Log.w("Login Error: NG");
    }

    context.sendJsonResponse(status, null);
  }

}
