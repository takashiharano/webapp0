/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import java.util.Map;
import java.util.Map.Entry;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.Group;
import com.takashiharano.webapp0.user.GroupManager;

public class GetGroupInfoListAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.hasPermission("sysadmin")) {
      context.sendJsonResponse("FORBIDDEN:GetGroupInfoList", null);
      return;
    }

    String status = "OK";
    GroupManager groupManager = context.getGroupManager();
    Map<String, Group> groups = groupManager.getAllGroupInfo();

    JsonBuilder jb = new JsonBuilder();
    jb.openList("grouplist");

    for (Entry<String, Group> entry : groups.entrySet()) {
      Group info = entry.getValue();
      String group = info.toJSON();
      jb.appendListElementAsObject(group);
    }

    jb.closeList();
    String json = jb.toString();
    context.sendJsonResponse(status, json);
  }

}
