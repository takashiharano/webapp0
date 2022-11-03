package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class DeleteUserAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String username = context.getRequestParameter("username");

    String status = "OK";
    try {
      UserManager userManager = context.getUserManager();
      userManager.deleteUser(username);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("User delete error: " + status);
    }

    context.sendJsonResponse(status, null);
  }

}
