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
    String username = context.getRequestParameter("username");
    String pwHash = context.getRequestParameter("pw");
    String fullname = context.getRequestParameter("fullname");
    String localFullName = context.getRequestParameter("localfullname");
    String email = context.getRequestParameter("email");
    String adminFlag = context.getRequestParameter("is_admin");
    String groups = context.getRequestParameter("groups");
    String privileges = context.getRequestParameter("privileges");
    String info1 = context.getRequestParameter("info1");
    String info2 = context.getRequestParameter("info2");
    String description = context.getRequestParameter("description");
    String userFlags = context.getRequestParameter("flags");

    if (!context.hasPermission("sysadmin")) {
      Log.i("AddUser: FORBIDDEN user=" + username);
      context.sendJsonResponse("FORBIDDEN:AddUser", null);
      return;
    }

    UserManager userManager = context.getUserManager();
    if (userManager.existsUser(username)) {
      Log.i("AddUser: USER_ALREADY_EXISTS username=" + username);
      context.sendJsonResponse("USER_ALREADY_EXISTS", null);
      return;
    }

    String status = "OK";
    try {
      userManager.regieterNewUser(username, pwHash, fullname, localFullName, email, adminFlag, groups, privileges, info1, info2, description, userFlags);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User regieter error: " + status);
    }

    Log.i("AddUser: " + status + " user=" + username);

    context.sendJsonResponse(status, null);
  }

}
