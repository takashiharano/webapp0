/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class DeleteUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String userId = context.getRequestParameter("uid");

    String currentUserId = context.getUserId();
    if (!context.hasPermission("sysadmin") || currentUserId.equals(userId)) {
      Log.w("DeleteUser: FORBIDDEN user=" + userId);
      context.sendJsonResponse("FORBIDDEN:DeleteUser", null);
      return;
    }

    String status = "OK";
    try {
      SessionManager sessionManager = context.getSessionManager();
      sessionManager.clearUserSessions(userId);

      UserManager userManager = context.getUserManager();
      userManager.deleteUser(userId);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User delete error: " + status);
    }

    Log.i("DeleteUser: " + status + " user=" + userId);

    context.sendJsonResponse(status, null);
  }

}
