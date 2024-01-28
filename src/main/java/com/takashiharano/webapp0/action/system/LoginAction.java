/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

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

    String username = context.getRequestParameter("id");
    UserManager userManager = context.getUserManager();
    if (isLocked(context, userManager, username)) {
      Log.w("Login: " + "LOCKED user=" + username);
      return "LOCKED";
    }

    String pwHash = context.getBSB64DecodedRequestParameter("pw");
    String result = userManager.authenticate(username, pwHash);
    String status;

    if ("OK".equals(result)) {
      status = "OK";
      SessionInfo sessionInfo = onLogin(context, sessionManager, userManager, username);
      Log.i("Login: OK user=" + username + " sid=" + sessionInfo.getShortSessionId());
    } else {
      String msg;
      status = "NG";
      if (("PASSWORD_MISMATCH".equals(result)) || ("EMPTY_VALUE".equals(result))) {
        msg = "NG user=" + username;
        userManager.incrementLoginFailedCount(username);
      } else if ("USER_NOT_FOUND".equals(result)) {
        status = "NG";
        msg = result;
      } else {
        status = "ERROR";
        msg = result;
      }
      Log.w("Login: " + msg);
    }

    return status;
  }

  private SessionInfo onLogin(ProcessContext context, SessionManager sessionManager, UserManager userManager, String username) throws Exception {
    long now = System.currentTimeMillis();
    SessionInfo sessionInfo = sessionManager.onLoggedIn(context, username);
    UserStatus userStatus = userManager.getUserStatusInfo(username);
    userStatus.setLastLogin(now);
    userManager.resetLoginFailedCount(username);
    return sessionInfo;
  }

  private boolean isLocked(ProcessContext context, UserManager userManager, String username) throws Exception {
    int loginFailedCount = userManager.getLoginFailedCount(username);
    long loginLockedTime = userManager.getLoginFailedTime(username);
    int loginFailureMaxCount = context.getConfigValueAsInteger("login_failure_max");
    long loginLockPeriodMillis = context.getConfigValueAsInteger("login_lock_period_sec") * 1000;

    if ((loginFailureMaxCount > 0) && (loginFailedCount >= loginFailureMaxCount)) {
      long now = System.currentTimeMillis();
      long elapsed = now - loginLockedTime;
      if ((loginLockPeriodMillis == 0) || (elapsed <= loginLockPeriodMillis)) {
        return true;
      } else {
        userManager.resetLoginFailedCount(username);
      }
    }

    return false;
  }

}
