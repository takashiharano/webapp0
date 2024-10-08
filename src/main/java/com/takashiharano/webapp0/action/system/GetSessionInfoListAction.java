/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.libutil.DateTime;
import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.session.SessionInfo;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.session.SessionTimelineLog;
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

    boolean includeLogs = context.getRequestParameterAsBoolean("logs", "1");
    long now = System.currentTimeMillis();

    jb.openList("sessions");
    for (Entry<String, SessionInfo> entry : sessions.entrySet()) {
      SessionInfo session = entry.getValue();
      String sid = session.getSessionId();
      String userId = session.getUserId();
      long lastAccessTime = session.getLastAccessTime();
      String addr = session.getRemoteAddr();
      String host = session.getRemoteHost();
      String ua = session.getUserAgent();
      long createdTime = session.getCreatedTime();
      String createdAddr = session.getCreatedRemoteAddr();
      String createdHost = session.getCreatedRemoteHost();
      String createdUserAgent = session.getCreatedUserAgent();

      User user = userManager.getUserInfo(userId);
      String fullName = user.getFullName();

      JsonBuilder jb1 = new JsonBuilder();
      jb1.append("sid", sid);
      jb1.append("uid", userId);
      jb1.append("time", lastAccessTime);
      jb1.append("user_fullname", fullName);
      jb1.append("addr", addr);
      jb1.append("host", host);
      jb1.append("ua", ua);
      jb1.append("c_time", createdTime);
      jb1.append("c_addr", createdAddr);
      jb1.append("c_host", createdHost);
      jb1.append("c_ua", createdUserAgent);

      if (includeLogs) {
        int targetOffset = context.getRequestParameterAsInteger("offset");
        List<SessionTimelineLog> tmLogs = getTimelineLogs(context, userId, sid, now, targetOffset);
        jb1.openList("timeline_log");
        for (int i = 0; i < tmLogs.size(); i++) {
          SessionTimelineLog tlLog = tmLogs.get(i);
          long time = tlLog.getTime();
          String info = tlLog.getInfo();
          JsonBuilder jb3 = new JsonBuilder();
          jb3.append("time", time);
          jb3.append("info", info);
          jb1.appendListElementAsObject(jb3.toString());
        }
        jb1.closeList();
      }

      jb.appendListElementAsObject(jb1.toString());
    }
    jb.closeList();

    String json = jb.toString();
    context.sendJsonResponse(status, json);
  }

  private List<SessionTimelineLog> getTimelineLogs(ProcessContext context, String userId, String sid, long now, int targetOffset) {
    long DAY_MILLIS = 86400000;
    long tm = now - DAY_MILLIS * targetOffset;
    long mnTimestamp = DateTime.getMidnightTimestamp(tm);
    long targetFrom = mnTimestamp;
    long targetTo = mnTimestamp + DAY_MILLIS;

    SessionManager sessionManager = context.getSessionManager();

    String[] logs = sessionManager.getUserTimelineLog(userId);
    List<SessionTimelineLog> tmLogs = new ArrayList<>();
    for (int i = 0; i < logs.length; i++) {
      String line = logs[i];
      String[] wk = line.split("\t");
      long logTime;
      String logSid;
      String info = null;
      try {
        logTime = Long.parseLong(wk[0]);
        logSid = wk[1];
        if (wk.length >= 3) {
          info = wk[2];
        }
      } catch (Exception e) {
        logTime = 0;
        logSid = "";
      }

      if ((logSid.equals(sid)) && (targetFrom <= logTime) && (logTime < targetTo)) {
        SessionTimelineLog tlLog = new SessionTimelineLog(logTime, logSid, info);
        tmLogs.add(tlLog);
      }
    }

    return tmLogs;
  }

}
