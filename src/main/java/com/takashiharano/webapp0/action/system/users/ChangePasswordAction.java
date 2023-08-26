/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class ChangePasswordAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");
    String pwHash = context.getRequestParameter("pw");

    String currentUsername = context.getUsername();
    if (!context.hasPermission("sysadmin") && !currentUsername.equals(username)) {
      Log.w("ChangePassword: FORBIDDEN user=" + username);
      context.sendJsonResponse("FORBIDDEN:ChangePassword", null);
      return;
    }

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.updateUser(username, pwHash, null, null, null, null, null, null, null);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("Change password error: " + status);
    }

    Log.i("ChangePassword: " + status + " user=" + username);

    context.sendJsonResponse(status, null);
  }

}
