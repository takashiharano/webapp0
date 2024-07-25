/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.session;

import com.libutil.JsonBuilder;
import com.libutil.StrUtil;

public class SessionInfo {

  private String sessionId;
  private String username;
  private long createdTime;
  private long lastAccessTime;
  private String remoteAddr;
  private String remoteHost;
  private String userAgent;

  public SessionInfo(String sessionId, String userName, String remoteAddr, String remoteHost, String userAgent) {
    this(sessionId, userName, System.currentTimeMillis(), 0, remoteAddr, remoteHost, userAgent);
  }

  public SessionInfo(String sessionId, String username, long createdTime, long lastAccessTime, String remoteAddr, String remoteHost, String userAgent) {
    this.sessionId = sessionId;
    this.username = username;
    this.createdTime = createdTime;
    this.lastAccessTime = lastAccessTime;
    this.remoteAddr = remoteAddr;
    this.remoteHost = remoteHost;
    this.userAgent = userAgent;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getShortSessionId() {
    return StrUtil.snip(sessionId, 7, 3);
  }

  public String getUsername() {
    return username;
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

  /**
   * Returns all properties in JSON.
   *
   * @return JSON string
   */
  public String toJSON() {
    JsonBuilder jb = new JsonBuilder();
    jb.append("sessionId", getSessionId());
    jb.append("username", getUsername());
    jb.append("createdTime", getCreatedTime());
    jb.append("lastAccessTime", getLastAccessTime());
    jb.append("remoteAddr", getRemoteAddr());
    jb.append("remoteHost", getRemoteHost());
    jb.append("userAgent", getUserAgent());

    String json = jb.toString();
    return json;
  }

}
