/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.session;

import com.libutil.StrUtil;

public class SessionInfo {

  private String sessionId;
  private String userId;
  private long lastAccessTime;
  private String remoteAddr;
  private String remoteHost;
  private String userAgent;
  private long createdTime;

  public SessionInfo(String sessionId, String userId, String remoteAddr, String remoteHost, String userAgent) {
    this(sessionId, userId, 0, remoteAddr, remoteHost, userAgent, System.currentTimeMillis());
  }

  public SessionInfo(String sessionId, String userId, long lastAccessTime, String remoteAddr, String remoteHost, String userAgent, long createdTime) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.lastAccessTime = lastAccessTime;
    this.remoteAddr = remoteAddr;
    this.remoteHost = remoteHost;
    this.userAgent = userAgent;
    this.createdTime = createdTime;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getShortSessionId() {
    return StrUtil.snip(sessionId, 7, 3);
  }

  public String getUserId() {
    return userId;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public long getLastAccessTime() {
    return lastAccessTime;
  }

  public void updateLastAccessTime(long timestamp) {
    this.lastAccessTime = timestamp;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

}
