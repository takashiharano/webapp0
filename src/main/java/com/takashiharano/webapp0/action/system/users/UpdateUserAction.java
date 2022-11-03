package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class UpdateUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");
    String pwHash = context.getRequestParameter("pw");
    String name = context.getRequestParameter("name");
    String adminFlg = context.getRequestParameter("isadmin");
    String permissions = context.getRequestParameter("permissions");

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.updateUser(username, pwHash, name, adminFlg, permissions);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User update error: " + status);
    }

    context.sendJsonResponse(status, null);
  }

}
