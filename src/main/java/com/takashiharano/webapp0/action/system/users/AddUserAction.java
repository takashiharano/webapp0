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

public class AddUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String userId = context.getRequestParameter("uid");
    String pwHash = context.getRequestParameter("pw");
    String fullname = context.getRequestParameter("fullname");
    String localFullName = context.getRequestParameter("localfullname");
    String aliasName = context.getRequestParameter("a_name");
    String email = context.getRequestParameter("email");
    String adminFlag = context.getRequestParameter("is_admin");
    String groups = context.getRequestParameter("groups");
    String privileges = context.getRequestParameter("privs");
    String info1 = context.getRequestParameter("info1");
    String info2 = context.getRequestParameter("info2");
    String info3 = context.getRequestParameter("info3");
    String description = context.getRequestParameter("desc");
    String userFlags = context.getRequestParameter("flags");

    if (!context.hasPermission("sysadmin")) {
      Log.i("AddUser: FORBIDDEN user=" + userId);
      context.sendJsonResponse("FORBIDDEN:AddUser", null);
      return;
    }

    UserManager userManager = context.getUserManager();
    if (userManager.existsUser(userId)) {
      Log.i("AddUser: USER_ALREADY_EXISTS user=" + userId);
      context.sendJsonResponse("USER_ALREADY_EXISTS", null);
      return;
    }

    String status = "OK";
    try {
      userManager.regieterNewUser(userId, pwHash, fullname, localFullName, aliasName, email, adminFlag, groups, privileges, info1, info2, info3, description, userFlags);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User regieter error: " + status);
    }

    Log.i("AddUser: " + status + " user=" + userId);

    context.sendJsonResponse(status, null);
  }

}
