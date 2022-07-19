package com.takashiharano.webapp0.action;

import com.libutil.FileUtil;
import com.libutil.HashUtil;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.auth.Auth;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.session.UserInfo;
import com.takashiharano.webapp0.util.Log;

public class LoginAction extends Action {

  private static Auth auth;

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
    String pwHash = HashUtil.sha256(pass + username);

    if (auth == null) {
      String homePath = appManager.getAppHomePath();
      String userPassFile = FileUtil.joinPath(homePath, "userpass.txt");
      auth = new Auth(userPassFile, 1);
    }

    if (StrUtil.isBlank(username) || StrUtil.isBlank(pass)) {
      context.sendJsonResponse("NG", null);
    }

    String status = "NG";

    UserInfo userInfo = null;
    if (appManager.isConfigTrue("pseudo_auth")) {
      userInfo = new UserInfo(username);
      userInfo.setAdministrator(true);
    } else {
      String result = auth.auth(username, pwHash);
      if ("OK".equals(result)) {
        userInfo = new UserInfo(username);
      } else if ("NG".equals(result)) {
        Log.i("LoginERR: " + username);
      }
    }

    if (userInfo != null) {
      Log.i("Login: " + username);
      status = "OK";
      sessionManager.onLoggedIn(context, userInfo);
    }

    context.sendJsonResponse(status, null);
  }

}
