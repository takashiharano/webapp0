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

public class EditUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");
    String pwHash = context.getRequestParameter("pw");
    String fullname = context.getRequestParameter("fullname");
    String localFullName = context.getRequestParameter("localfullname");
    String adminFlag = context.getRequestParameter("is_admin");
    String groups = context.getRequestParameter("groups");
    String privileges = context.getRequestParameter("privileges");
    String description = context.getRequestParameter("description");
    String userStatus = context.getRequestParameter("status");

    String currentUsername = context.getUsername();
    if (!context.hasPermission("sysadmin") && !currentUsername.equals(username)) {
      Log.w("EditUser: FORBIDDEN username=" + username);
      context.sendJsonResponse("FORBIDDEN:EditUser", null);
      return;
    }

    String status = "OK";
    String info = null;
    try {
      UserManager userManager = context.getUserManager();
      userManager.updateUser(username, pwHash, fullname, localFullName, adminFlag, groups, privileges, description, userStatus);
      if (pwHash != null) {
        info = "PW changed";
      }
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User regieter error: " + status);
    }

    Log.i("EditUser: " + status + " username=" + username + ((info == null) ? "" : " " + info));

    context.sendJsonResponse(status, null);
  }

}
