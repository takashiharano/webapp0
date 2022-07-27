package com.takashiharano.webapp0.session;

public class SessionInfo {

  private String sessionId;
  private String username;
  private long createdTime;
  private long lastAccessedTime;
  private String remoteAddr;
  private String userAgent;

  public SessionInfo(String sessionId, String userName, String remoteAddr, String userAgent) {
    this(sessionId, userName, System.currentTimeMillis(), 0, remoteAddr, userAgent);
  }

  public SessionInfo(String sessionId, String username, long createdTime, long lastAccessedTime, String remoteAddr, String userAgent) {
    this.sessionId = sessionId;
    this.username = username;
    this.createdTime = createdTime;
    this.lastAccessedTime = lastAccessedTime;
    this.remoteAddr = remoteAddr;
    this.userAgent = userAgent;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUsername() {
    return username;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public long getLastAccessedTime() {
    return lastAccessedTime;
  }

  public void updateLastAccessedTime() {
    this.lastAccessedTime = System.currentTimeMillis();
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

}
