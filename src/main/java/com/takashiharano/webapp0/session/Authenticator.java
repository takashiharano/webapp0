package com.takashiharano.webapp0.session;

import com.takashiharano.webapp0.ProcessContext;

public class Authenticator {

  public static boolean checkAuthorization(ProcessContext context) {
    SessionInfo sessionInfo = context.getSessionInfo();
    if (sessionInfo == null) {
      return false;
    }
    return true;
  }

}
