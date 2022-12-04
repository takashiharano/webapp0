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
    String name = context.getRequestParameter("name");
    String adminFlag = context.getRequestParameter("isadmin");
    String permissions = context.getRequestParameter("permissions");
    String userStatus = context.getRequestParameter("status");

    String currentUsername = context.getUserName();
    if (!context.isAdministrator() && !currentUsername.equals(username)) {
      context.sendJsonResponse("FORBIDDEN:Addser", null);
      return;
    }

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.updateUser(username, pwHash, adminFlag, name, permissions, userStatus);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User regieter error: " + status);
    }

    context.sendJsonResponse(status, null);
  }

}
