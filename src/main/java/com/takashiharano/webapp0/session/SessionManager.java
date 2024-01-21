/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.session;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.libutil.FileUtil;
import com.libutil.HashUtil;
import com.libutil.RandomGenerator;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.Log;

public class SessionManager {

  public static final String SESSION_COOKIE_NAME = AppManager.MODULE_NAME + "_sid";

  private String sessionInfoFilePath;
  private ConcurrentHashMap<String, SessionInfo> sessionMap;

  public SessionManager(String sessionFilePath) {
    sessionMap = new ConcurrentHashMap<>();
    this.sessionInfoFilePath = sessionFilePath;
  }

  /**
   * Updates last accessed time.
   *
   * @param context
   *          Process Context
   */
  public void onAccess(ProcessContext context, long timestamp) {
    String sessinId = context.getSessionId();
    if (sessinId == null) {
      return;
    }

    SessionInfo sessionInfo = sessionMap.get(sessinId);
    if (sessionInfo == null) {
      return;
    }

    sessionInfo.updateLastAccessedTime(timestamp);

    String remoteAddr = context.getRemoteAddress(true);
    sessionInfo.setRemoteAddr(remoteAddr);

    String ua = context.getUserAgent();
    sessionInfo.setUserAgent(ua);
  }

  /**
   * Callback for the web application shut down.<br>
   * This is called by AppManager#onStop().
   */
  public void onStop() {
    saveSessionInfo(sessionInfoFilePath);
  }

  /**
   * Returns the cookie name for session ID.
   *
   * @return the cookie name
   */
  public String getSessionCoolieName() {
    return SESSION_COOKIE_NAME;
  }

  /**
   * Registers a session info.
   *
   * @param info
   *          the session info
   */
  public void registerSessionInfo(SessionInfo info) {
    String sessionId = info.getSessionId();
    sessionMap.put(sessionId, info);
  }

  /**
   * Returns the session info corresponding to the given ID.
   *
   * @param sessionId
   *          target session ID
   * @return the session info. if it does not exist, returns null.
   */
  public SessionInfo getSessionInfo(String sessionId) {
    SessionInfo info = sessionMap.get(sessionId);
    return info;
  }

  /**
   * Returns the session info corresponding to the current context.
   *
   * @param context
   *          the request process context.
   * @return the session info. if it does not exist, returns null.
   */
  public SessionInfo getSessionInfo(ProcessContext context) {
    String sid = context.getSessionId();
    if (sid == null) {
      return null;
    }
    return getSessionInfo(sid);
  }

  /**
   * Returns the session timeout in seconds.<br>
   * The value is defined in app.properties with "session_timeout_sec" field.
   *
   * @return timeout in seconds
   */
  public int getSessionTimeout() {
    AppManager appManager = AppManager.getInstance();
    int timeout = appManager.getConfigValueAsInteger("session_timeout_sec");
    return timeout;
  }

  /**
   * Returns the session info map.
   *
   * @return the session info map
   */
  public ConcurrentHashMap<String, SessionInfo> getSessionMap() {
    return sessionMap;
  }

  /**
   * Removes the session info from the session map.
   *
   * @param sessionId
   *          target session id
   */
  public void removeSessionInfo(String sessionId) {
    sessionMap.remove(sessionId);
  }

  /**
   * Removes the session info associated with the user name from the session map.
   *
   * @param username
   *          target user name
   */
  public void removeSessionInfoByUsername(String username) {
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      String sUsername = sessionInfo.getUsername();
      if (sUsername.equals(username)) {
        sessionMap.remove(sessionId);
      }
    }
  }

  /**
   * Loads session info from a file.
   *
   * @param path
   *          Session info file path
   */
  public void loadSessionInfo(String path) {
    String[] records = FileUtil.readTextAsArray(path);
    if (records == null) {
      return;
    }

    int count = 0;
    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      try {
        restoreSessionInfo(record);
        count++;
      } catch (Exception e) {
        Log.e("Session restore error: " + e.toString());
      }
    }

    Log.i(count + " session info loaded");
  }

  /**
   * Restores the session info to memory
   *
   * @param record
   *          session record
   */
  private void restoreSessionInfo(String record) {
    String[] fields = record.split("\t");
    String sessionId = fields[0];
    String username = fields[1];
    String sCreatedTime = fields[2];
    String sLastAccessedTime = fields[3];
    String remoteAddr = fields[4];
    String remoteHost = fields[5];
    String userAgent = fields[6];
    long createdTime = Long.parseLong(sCreatedTime);
    long lastAccessedTime = Long.parseLong(sLastAccessedTime);

    SessionInfo info = new SessionInfo(sessionId, username, createdTime, lastAccessedTime, remoteAddr, remoteHost, userAgent);
    registerSessionInfo(info);
  }

  /**
   * Saves session info into the file.
   *
   * @param path
   *          the file path to save session info
   */
  public void saveSessionInfo(String path) {
    // Remove invalid info prior to save.
    cleanInvalidatedSessionInfo();

    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      count++;
      String sessionId = entry.getKey();
      SessionInfo info = sessionMap.get(sessionId);
      String username = info.getUsername();
      long createdTime = info.getCreatedTime();
      long lastAccessedTime = info.getLastAccessedTime();
      String remoteAddr = info.getRemoteAddr();
      String remoteHost = info.getRemoteHost();
      String userAgent = info.getUserAgent();

      // sessionId,username,accessToken,createdTime,lastAccessedTime,lastAccessedRemoteAddr,host,ua
      StringBuilder record = new StringBuilder();
      record.append(sessionId);
      record.append("\t");
      record.append(username);
      record.append("\t");
      record.append(createdTime);
      record.append("\t");
      record.append(lastAccessedTime);
      record.append("\t");
      record.append(remoteAddr);
      record.append("\t");
      record.append(remoteHost);
      record.append("\t");
      record.append(userAgent);

      sb.append(record.toString());
      sb.append("\n");
    }

    String sessions = sb.toString();
    try {
      Log.i("Writing session info: " + path);
      FileUtil.write(path, sessions);
      Log.i(count + " session info saved");
    } catch (IOException e) {
      Log.e("Session info save error", e);
    }
  }

  /**
   * Process on Login.
   *
   * @param context
   *          Process Context
   * @param username
   *          the user name
   */
  public void onLoggedIn(ProcessContext context, String username) {
    SessionInfo session = getSessionInfo(context);

    if (session != null) {
      String sessionId = session.getSessionId();
      removeSessionInfo(sessionId);
    }

    createNewSession(context, username);
  }

  /**
   * Creates a new session.
   *
   * @param context
   *          Process Context
   * @param username
   *          the user name
   */
  private void createNewSession(ProcessContext context, String username) {
    // Recreate session
    HttpServletRequest request = context.getRequest();
    HttpSession session = request.getSession();
    session.invalidate();
    session = request.getSession(true);
    String sessionId = generateSessionId(username);
    String remoteAddr = context.getRemoteAddr();
    String remoteHost = context.getRemoteHost();
    String userAgent = context.getUserAgent();

    long createdTime = System.currentTimeMillis();
    long lastAccessedTime = 0L;

    SessionInfo info = new SessionInfo(sessionId, username, createdTime, lastAccessedTime, remoteAddr, remoteHost, userAgent);
    registerSessionInfo(info);

    // Set session expiration
    int sessionTimeoutSec = getSessionTimeout();
    session.setMaxInactiveInterval(sessionTimeoutSec);
    context.setSessionCookieMaxAge(sessionId, sessionTimeoutSec);
  }

  /**
   * Generate a session ID.
   *
   * @return Session ID
   */
  private String generateSessionId(String username) {
    long t = System.currentTimeMillis();
    long r = RandomGenerator.getLong();
    String s = t + username + r;
    String sessionId = HashUtil.getHashString(s, "SHA-256");
    return sessionId;
  }

  /**
   * Removes expired session info from the management map.
   */
  public void cleanInvalidatedSessionInfo() {
    long now = System.currentTimeMillis();
    AppManager appManager = AppManager.getInstance();
    int sessionTimeoutSec = appManager.getConfigValueAsInteger("session_timeout_sec");
    long timeoutMillis = sessionTimeoutSec * 1000;
    int count = 0;
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      long lastAccessedTime = sessionInfo.getLastAccessedTime();
      long elapsed = now - lastAccessedTime;
      if (elapsed > timeoutMillis) {
        sessionMap.remove(sessionId);
        count++;
      }
    }
    if (count > 0) {
      Log.i(count + " session removed");
    }
  }

  /**
   * Logout.
   *
   * @param sessionId
   *          target session id
   * @return true if logged out successfully.
   */
  public boolean logout(String sessionId) {
    SessionInfo info = sessionMap.get(sessionId);
    if (info == null) {
      Log.e("sid " + sessionId + " was not found");
      return false;
    }
    String username = info.getUsername();
    removeSessionInfo(sessionId);
    cleanInvalidatedSessionInfo();
    Log.i("Logout: user=" + username);
    return true;
  }

  /**
   * Logout.
   *
   * @param context
   *          Process Context
   */
  public void logout(ProcessContext context) {
    String username = context.getUsername();
    HttpSession httpSession = context.getHttpSession();
    String sessionId = context.getSessionId();
    removeSessionInfo(sessionId);
    httpSession.invalidate();
    invalidateSessionCookie(context);
    cleanInvalidatedSessionInfo();
    Log.i("Logout: user=" + username);
  }

  /**
   * Clear all user sessions.
   *
   * @param username
   *          target username
   */
  public void clearUserSessions(String username) {
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      String uName = sessionInfo.getUsername();
      if (uName.equals(username)) {
        sessionMap.remove(sessionId);
      }
    }
  }

  private void invalidateSessionCookie(ProcessContext context) {
    Cookie cookie = new Cookie(SESSION_COOKIE_NAME, "");
    cookie.setMaxAge(0);
    HttpServletResponse response = context.getResponse();
    response.addCookie(cookie);
  }

}
