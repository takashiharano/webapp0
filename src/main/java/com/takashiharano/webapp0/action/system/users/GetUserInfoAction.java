/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.action.system.users;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserInfo;
import com.takashiharano.webapp0.user.UserManager;

public class GetUserInfoAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");

    String currentUsername = context.getUsername();
    if (!context.isAdministrator() && !currentUsername.equals(username)) {
      context.sendJsonResponse("FORBIDDEN:GetUserInfo", null);
      return;
    }

    String status = "OK";
    UserManager userManager = context.getUserManager();
    UserInfo userInfo = userManager.getUserInfo(username);
    if (userInfo == null) {
      context.sendJsonResponse("USER_NOT_FOUND", null);
      return;
    }

    JsonBuilder jb = new JsonBuilder();
    jb.append("username", userInfo.getUsername());
    jb.append("fullname", userInfo.getFullName());
    jb.append("isAdmin", userInfo.isAdmin());
    jb.append("privileges", userInfo.getPrivilegesInOneLine());
    jb.append("status", userInfo.getStatus());
    String json = jb.toString();

    context.sendJsonResponse(status, json);
  }

}
