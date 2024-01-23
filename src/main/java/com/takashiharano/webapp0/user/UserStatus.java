/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.session.SessionManager;

public class UserStatus {

  private String username;
  private long lastAccessed;
  private long pwChangedTime;
  private int loginFailedCount;
  private long loginFailedTime;

  public UserStatus(String username) {
    this(username, 0L, 0L, 0, 0L);
  }

  public UserStatus(String username, long lastAccessed, long pwChangedTime, int loginFailedCount, long loginFailedTime) {
    this.username = username;
    this.lastAccessed = lastAccessed;
    this.pwChangedTime = pwChangedTime;
    this.loginFailedCount = loginFailedCount;
    this.loginFailedTime = loginFailedTime;
  }

  public long getLastAccessed() {
    return lastAccessed;
  }

  public void setLastAccessed(long lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  public long getPwChangedTime() {
    return pwChangedTime;
  }

  public void setPwChangedTime(long pwChangedTime) {
    this.pwChangedTime = pwChangedTime;
  }

  public int getLoginFailedCount() {
    return loginFailedCount;
  }

  public void setLoginFailedCount(int loginFailedCount) {
    this.loginFailedCount = loginFailedCount;
  }

  public void incrementLoginFailedCount() {
    this.loginFailedCount++;
  }

  public void resetLoginFailedCount() {
    this.loginFailedCount = 0;
  }

  public long getLoginFailedTime() {
    return loginFailedTime;
  }

  public void setLoginFailedTime(long loginFailedTime) {
    this.loginFailedTime = loginFailedTime;
  }

  public int getSessionCount() {
    AppManager appManager = AppManager.getInstance();
    SessionManager sessionManager = appManager.getSessionManager();
    int count = sessionManager.getSessionCount(username);
    return count;
  }

  public String toJSON() {
    int sessionCount = getSessionCount();
    JsonBuilder jb = new JsonBuilder();
    jb.append("last_accessed", lastAccessed);
    jb.append("pw_changed_at", pwChangedTime);
    jb.append("login_failed_count", loginFailedCount);
    jb.append("login_failed_time", loginFailedTime);
    jb.append("sessions", sessionCount);
    String json = jb.toString();
    return json;
  }

}
