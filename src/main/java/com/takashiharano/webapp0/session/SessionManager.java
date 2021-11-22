package com.takashiharano.webapp0.session;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.libutil.FileUtil;
import com.libutil.RandomGenerator;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.Log;

public class SessionManager {

  public static final String SESSION_COOKIE_NAME = AppManager.MODULE_NAME + "-sid";

  private String sessionInfoFilePath;
  private ConcurrentHashMap<String, SessionInfo> sessionMap;

  public SessionManager(String sessionFilePath) {
    sessionMap = new ConcurrentHashMap<>();
    this.sessionInfoFilePath = sessionFilePath;

    if (FileUtil.exists(sessionFilePath)) {
      loadSessionInfo(sessionInfoFilePath);
    }
  }

  /**
   * Updates last accessed time.
   *
   * @param context
   *          Process Context
   */
  public void onAccess(ProcessContext context) {
    String sessinId = context.getSessionId();
    if (sessinId == null) {
      return;
    }

    SessionInfo sessionInfo = sessionMap.get(sessinId);
    if (sessionInfo == null) {
      return;
    }

    sessionInfo.updateLastAccessedTime();

    String remoteAddr = context.getRemoteAddress(true);
    sessionInfo.setRemoteAddr(remoteAddr);
  }

  public void onStop() {
    saveSessionInfo(sessionInfoFilePath);
  }

  public String getSessionCoolieName() {
    return SESSION_COOKIE_NAME;
  }

  public void addSessionInfo(String sessionId, String userName, String remoteAddr, String usrAgent) {
    addSessionInfo(sessionId, userName, System.currentTimeMillis(), 0, remoteAddr, usrAgent);
  }

  public void addSessionInfo(String sessionId, String userName, long createdTime, long lastAccessedTime,
      String remoteAddr, String usrAgent) {
    SessionInfo info = new SessionInfo(sessionId, userName, createdTime, lastAccessedTime, remoteAddr, usrAgent);
    sessionMap.put(sessionId, info);
  }

  public SessionInfo getSessionInfo(String sessionId) {
    SessionInfo info = sessionMap.get(sessionId);
    return info;
  }

  public SessionInfo getSessionInfo(ProcessContext context) {
    String sid = context.getSessionId();
    if (sid == null) {
      return null;
    }
    return getSessionInfo(sid);
  }

  public ConcurrentHashMap<String, SessionInfo> getSessionMap() {
    return sessionMap;
  }

  public String getUserName(ProcessContext context) {
    String sessionId = context.getSessionId();
    if (sessionId == null) {
      return null;
    }
    SessionInfo info = getSessionInfo(sessionId);
    if (info == null) {
      return null;
    }
    String userName = info.getUsername();
    return userName;
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

    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      String[] fields = record.split("\t");
      String sessionId = fields[0];
      String userName = fields[1];
      String sCreatedTime = fields[2];
      String sLastAccessedTime = fields[3];
      String remoteAddr = fields[4];
      String userAgent = fields[5];
      long createdTime = Long.parseLong(sCreatedTime);
      long lastAccessedTime = Long.parseLong(sLastAccessedTime);

      // Restores the session info to memory
      addSessionInfo(sessionId, userName, createdTime, lastAccessedTime, remoteAddr, userAgent);
    }

    Log.i(records.length + " session info loaded");
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
      String userName = info.getUsername();
      long createdTime = info.getCreatedTime();
      long lastAccessedTime = info.getLastAccessedTime();
      String remoteAddr = info.getRemoteAddr();
      String userAgent = info.getUserAgent();

      // sessionId,userName,accessToken,createdTime,lastAccessedTime,lastAccessedRemoteAddr
      StringBuilder record = new StringBuilder();
      record.append(sessionId);
      record.append("\t");
      record.append(userName);
      record.append("\t");
      record.append(createdTime);
      record.append("\t");
      record.append(lastAccessedTime);
      record.append("\t");
      record.append(remoteAddr);
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
      Log.e("Save session info error", e);
    }
  }

  public void removeSessionInfo(String sessionId) {
    sessionMap.remove(sessionId);
  }

  /**
   * Process on Login.
   *
   * @param context
   *          Process Context
   * @param userName
   *          the user name
   */
  public void onLoggedIn(ProcessContext context, UserInfo userInfo) {
    String username = userInfo.getUsername();
    removeSessionInfoByUsername(username);
    createNewSession(context, userInfo);
  }

  /**
   * Creates a new session.
   *
   * @param context
   *          Process Context
   * @param userInfo
   *          the user info
   */
  private void createNewSession(ProcessContext context, UserInfo userInfo) {
    String username = userInfo.getUsername();

    // Recreate session
    HttpServletRequest request = context.getRequest();
    HttpSession session = request.getSession();
    session.invalidate();
    session = request.getSession(true);
    String sessionId = generateSessionId();
    String remoteAddr = context.getRemoteAddr();
    String userAgent = context.getUserAgent();
    addSessionInfo(sessionId, username, remoteAddr, userAgent);

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
  private String generateSessionId() {
    String SESSION_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    String sessionId = RandomGenerator.getString(SESSION_ID_CHARS, 24);
    return sessionId;
  }

  private int getSessionTimeout() {
    AppManager appManager = AppManager.getInstance();
    int timeout = appManager.getConfigIntValue("session_timeout_sec");
    return timeout;
  }

  private void invalidateSessionCookie(ProcessContext context) {
    Cookie cookie = new Cookie(SESSION_COOKIE_NAME, "");
    cookie.setMaxAge(0);
    HttpServletResponse response = context.getResponse();
    response.addCookie(cookie);
  }

  /**
   * Removes expired session info from the management map.
   */
  public void cleanInvalidatedSessionInfo() {
    long now = System.currentTimeMillis();
    AppManager appManager = AppManager.getInstance();
    int sessionTimeoutSec = appManager.getConfigIntValue("session_timeout_sec");
    long timeoutMillis = sessionTimeoutSec * 1000;
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      long lastAccessedTime = sessionInfo.getLastAccessedTime();
      if (now - lastAccessedTime > timeoutMillis) {
        sessionMap.remove(sessionId);
      }
    }
  }

  /**
   * Removes the session info associated with the user name from the session map.
   *
   * @param username
   *          the user name
   */
  private void removeSessionInfoByUsername(String username) {
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
   * Logout.
   *
   * @param context
   *          Process Context
   */
  public void logout(ProcessContext context) {
    String username = context.getUserName();
    Log.i("Logout: " + username);
    HttpSession session = context.getSession();
    String sessionId = context.getSessionId();
    removeSessionInfo(sessionId);
    session.invalidate();
    invalidateSessionCookie(context);
    cleanInvalidatedSessionInfo();
  }

}
