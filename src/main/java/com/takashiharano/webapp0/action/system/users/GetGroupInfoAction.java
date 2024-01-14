/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.Group;
import com.takashiharano.webapp0.user.GroupManager;

public class GetGroupInfoAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String gid = context.getRequestParameter("gid");

    if (!context.hasPermission("sysadmin")) {
      context.sendJsonResponse("FORBIDDEN:GetGroupInfo", null);
      return;
    }

    String status = "OK";
    GroupManager groupManager = context.getGroupManager();
    Group group = groupManager.getGroupInfo(gid);
    if (group == null) {
      context.sendJsonResponse("GROUP_NOT_FOUND", null);
      return;
    }

    String json = group.toJSON();

    context.sendJsonResponse(status, json);
  }

}
