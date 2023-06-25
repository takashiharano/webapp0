/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import java.util.Map;
import java.util.Map.Entry;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserInfo;
import com.takashiharano.webapp0.user.UserManager;

public class GetUserInfoListAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.isAdmin()) {
      context.sendJsonResponse("FORBIDDEN:GetUserInfoList", null);
      return;
    }

    String status = "OK";
    UserManager userManager = context.getUserManager();
    Map<String, UserInfo> users = userManager.getAllUserInfo();

    JsonBuilder jb = new JsonBuilder();
    jb.openList("userlist");

    for (Entry<String, UserInfo> entry : users.entrySet()) {
      UserInfo info = entry.getValue();
      String user = UserInfoCommonLogic.buildUserInfoJson(info);
      jb.appendListElementAsObject(user);
    }

    jb.closeList();
    String json = jb.toString();
    context.sendJsonResponse(status, json);
  }

}
