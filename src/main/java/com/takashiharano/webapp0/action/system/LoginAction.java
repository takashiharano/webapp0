/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.action.system;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
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
    SessionManager sessionManager = context.getSessionManager();

    sessionManager.cleanInvalidatedSessionInfo();

    String username = context.getRequestParameter("id");
    String pass = context.getBSB64DecodedRequestParameter("pw");

    UserManager userManager = context.getUserManager();
    String result = userManager.authenticate(username, pass);
    String status;

    if ("OK".equals(result)) {
      status = "OK";
      Log.i("Login: OK user=" + username);
      status = "OK";
      sessionManager.onLoggedIn(context, username);
    } else {
      String msg;
      status = "NG";
      if (("PASSWORD_MISMATCH".equals(result)) || ("EMPTY_VALUE".equals(result))) {
        msg = "NG user=" + username;
      } else if ("USER_NOT_FOUND".equals(result)) {
        status = "NG";
        msg = result;
      } else {
        status = "ERROR";
        msg = result;
      }
      Log.w("Login: " + msg);
    }

    context.sendJsonResponse(status, null);
  }

}
