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
    String userId = context.getRequestParameter("uid");

    String currentUsername = context.getUserId();
    if (!context.hasPermission("sysadmin") || currentUsername.equals(userId)) {
      Log.w("UnlockUser: FORBIDDEN user=" + userId);
      context.sendJsonResponse("FORBIDDEN:UnlockUser", null);
      return;
    }

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.unlockUser(userId);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User unlock error: " + status);
    }

    Log.i("UnlockUser: " + status + " user=" + userId);

    context.sendJsonResponse(status, null);
  }

}
