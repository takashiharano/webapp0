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

public class EditGroupAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String gid = context.getRequestParameter("gid");
    String name = context.getRequestParameter("name");
    String privileges = context.getRequestParameter("privs");
    String description = context.getRequestParameter("desc");

    if (!context.hasPermission("sysadmin")) {
      Log.w("EditGroup: FORBIDDEN gid=" + gid);
      context.sendJsonResponse("FORBIDDEN:EditGroup", null);
      return;
    }

    String status = "OK";
    GroupManager groupManager = context.getGroupManager();
    try {
      groupManager.updateGroup(gid, name, privileges, description);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("Group edit error: " + status);
    }

    Log.i("EditGroup: " + status + " gid=" + gid);

    context.sendJsonResponse(status, null);
  }

}
