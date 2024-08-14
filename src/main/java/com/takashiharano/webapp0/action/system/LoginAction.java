/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import java.io.IOException;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.session.SessionInfo;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.user.UserStatus;
import com.takashiharano.webapp0.util.Log;

public class LoginAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    String status = login(context);
    context.sendJsonResponse(status, null);
  }

  private String login(ProcessContext context) throws Exception {
    SessionManager sessionManager = context.getSessionManager();

    String userId = context.getRequestParameter("id");
    UserManager userManager = context.getUserManager();

    String pwHash = context.getBSB64DecodedRequestParameter("pw");
    String result = userManager.authenticate(context, userId, pwHash);

    String status;

    if ("OK".equals(result)) {
      status = "OK";
      SessionInfo sessionInfo = onLogin(context, sessionManager, userManager, userId);
      Log.i("Login: OK user=" + userId + " sid=" + sessionInfo.getShortSessionId());
    } else {
      String msg;
      status = "NG";
      if (("PASSWORD_MISMATCH".equals(result)) || ("EMPTY_VALUE".equals(result))) {
        msg = "NG user=" + userId;
        userManager.incrementLoginFailedCount(userId);
      } else if ("USER_NOT_FOUND".equals(result)) {
        status = "NG";
        msg = result;
      } else if ("DISABLED".equals(result)) {
        Log.w("Login: " + "DISABLED user=" + userId);
        return "DISABLED";
      } else if ("LOCKED".equals(result)) {
        Log.w("Login: " + "LOCKED user=" + userId);
        return "LOCKED";
      } else {
        status = "ERROR";
        msg = result;
      }
      Log.w("Login: " + msg);
    }

    return status;
  }

  private SessionInfo onLogin(ProcessContext context, SessionManager sessionManager, UserManager userManager, String userId) throws Exception {
    long now = System.currentTimeMillis();
    SessionInfo sessionInfo = sessionManager.onLoggedIn(context, userId);
    UserStatus userStatus = userManager.getUserStatusInfo(userId);
    userStatus.setLastLogin(now);
    userManager.resetLoginFailedCount(userId);

    String sessionId = sessionInfo.getSessionId();
    try {
      sessionManager.saveTimelineLog(userId, sessionId, now, "LOGIN");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return sessionInfo;
  }

}
