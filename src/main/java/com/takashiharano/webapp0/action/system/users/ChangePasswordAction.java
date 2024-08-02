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
    String userId = context.getRequestParameter("uid");
    String pwHash = context.getRequestParameter("pw");

    String currentUid = context.getUserId();
    if (!context.hasPermission("sysadmin") && !currentUid.equals(userId)) {
      Log.w("ChangePassword: FORBIDDEN user=" + userId);
      context.sendJsonResponse("FORBIDDEN:ChangePassword", null);
      return;
    }

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.changePassword(userId, pwHash);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("Change password error: " + status);
    }

    Log.i("ChangePassword: " + status + " user=" + userId);

    context.sendJsonResponse(status, null);
  }

}
