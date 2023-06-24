/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
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
    String username = context.getRequestParameter("username");

    String currentUsername = context.getUsername();
    if (!context.isAdministrator() && !currentUsername.equals(username)) {
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
      String user = buildUserInfoJson(info);
      jb.appendListElementAsObject(user);
    }

    jb.closeList();
    String json = jb.toString();
    context.sendJsonResponse(status, json);
  }

  private String buildUserInfoJson(UserInfo info) {
    JsonBuilder jb = new JsonBuilder();
    jb.append("username", info.getUsername());
    jb.append("fullname", info.getFullName());
    jb.append("isAdmin", info.isAdmin());
    jb.append("privileges", info.getPrivilegesInOneLine());
    jb.append("status", info.getStatus());
    String json = jb.toString();
    return json;
  }

}
