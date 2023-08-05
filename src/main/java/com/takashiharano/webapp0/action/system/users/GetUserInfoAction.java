/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.User;
import com.takashiharano.webapp0.user.UserManager;

public class GetUserInfoAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");

    String currentUsername = context.getUsername();
    if (!context.hasPermission("sysadmin") && !currentUsername.equals(username)) {
      context.sendJsonResponse("FORBIDDEN:GetUserInfo", null);
      return;
    }

    String status = "OK";
    UserManager userManager = context.getUserManager();
    User user = userManager.getUserInfo(username);
    if (user == null) {
      context.sendJsonResponse("USER_NOT_FOUND", null);
      return;
    }

    String json = user.toJSON();

    context.sendJsonResponse(status, json);
  }

}
