/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.GroupManager;
import com.takashiharano.webapp0.util.Log;

public class DeleteGroupAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String gid = context.getRequestParameter("gid");

    if (!context.hasPermission("sysadmin")) {
      Log.w("DeleteGroup: FORBIDDEN group=" + gid);
      context.sendJsonResponse("FORBIDDEN:DeleteGroup", null);
      return;
    }

    String status = "OK";
    try {
      GroupManager groupManager = context.getGroupManager();
      groupManager.deleteGroup(gid);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("Group delete error: " + status);
    }

    Log.i("DeleteGroup: " + status + " gid=" + gid);

    context.sendJsonResponse(status, null);
  }

}
