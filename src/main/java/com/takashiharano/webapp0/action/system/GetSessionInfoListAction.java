/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import java.util.Map;
import java.util.Map.Entry;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.session.SessionInfo;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.user.User;
import com.takashiharano.webapp0.user.UserManager;

public class GetSessionInfoListAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.hasPermission("sysadmin")) {
      context.sendJsonResponse("FORBIDDEN:GetSessionInfoList", null);
      return;
    }

    String status = "OK";
    SessionManager sessionManager = context.getSessionManager();
    Map<String, SessionInfo> sessions = sessionManager.getSessionMap();
    UserManager userManager = context.getUserManager();
    String currentSid = context.getSessionId();

    JsonBuilder jb = new JsonBuilder();
    jb.append("currentSid", currentSid);

    jb.openList("sessions");
    for (Entry<String, SessionInfo> entry : sessions.entrySet()) {
      SessionInfo info = entry.getValue();
      String sid = info.getSessionId();
      String username = info.getUsername();
      String addr = info.getRemoteAddr();
      String ua = info.getUserAgent();
      long createdTime = info.getCreatedTime();
      long lastAccessTime = info.getLastAccessTime();

      User user = userManager.getUserInfo(username);
      String fullName = user.getFullName();

      JsonBuilder jb1 = new JsonBuilder();
      jb1.append("sid", sid);
      jb1.append("username", username);
      jb1.append("fullName", fullName);
      jb1.append("createdTime", createdTime);
      jb1.append("lastAccessTime", lastAccessTime);
      jb1.append("addr", addr);
      jb1.append("ua", ua);

      jb.appendListElementAsObject(jb1.toString());
    }
    jb.closeList();

    String json = jb.toString();
    context.sendJsonResponse(status, json);
  }

}
