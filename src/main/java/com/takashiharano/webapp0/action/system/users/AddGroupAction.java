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

public class AddGroupAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String gid = context.getRequestParameter("gid");
    String name = context.getRequestParameter("name");
    String privileges = context.getRequestParameter("privileges");
    String description = context.getRequestParameter("description");

    if (!context.hasPermission("sysadmin")) {
      Log.i("AddGroup: FORBIDDEN gid=" + gid);
      context.sendJsonResponse("FORBIDDEN:AddGroup", null);
      return;
    }

    GroupManager groupManager = context.getGroupManager();
    if (groupManager.existsGroup(gid)) {
      Log.i("AddGroup: GROUP_ALREADY_EXISTS gid=" + gid);
      context.sendJsonResponse("GROUP_ALREADY_EXISTS", null);
      return;
    }

    String status = "OK";
    try {
      groupManager.regieterNewGroup(gid, name, privileges, description);
    } catch (Exception e) {
      status = e.getMessage();
      Log.e("Group regieter error: " + status);
    }

    Log.i("AddGroup: " + status + " gid=" + gid);

    context.sendJsonResponse(status, null);
  }

}
