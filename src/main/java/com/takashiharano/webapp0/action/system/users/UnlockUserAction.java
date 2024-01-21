/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class UnlockUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");

    String currentUsername = context.getUsername();
    if (!context.hasPermission("sysadmin") || currentUsername.equals(username)) {
      Log.w("UnlockUser: FORBIDDEN user=" + username);
      context.sendJsonResponse("FORBIDDEN:UnlockUser", null);
      return;
    }

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.unlockUser(username);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User unlock error: " + status);
    }

    Log.i("UnlockUser: " + status + " user=" + username);

    context.sendJsonResponse(status, null);
  }

}
