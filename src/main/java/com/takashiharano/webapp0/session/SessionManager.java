/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.libutil.CsvBuilder;
import com.libutil.FileUtil;
import com.libutil.HashUtil;
import com.libutil.Randomizer;
import com.libutil.RingBuffer;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.user.UserStatus;
import com.takashiharano.webapp0.util.CsvFieldGetter;
import com.takashiharano.webapp0.util.Log;

public class SessionManager {

  public static final String SESSION_COOKIE_NAME = AppManager.MODULE_NAME + "_sid";
  private static final int DEFAULT_MAX_SESSIONS_PER_USER = 10;
  private static final String TIMELINE_LOG_FILE_NAME = "timeline.log";

  private ConcurrentHashMap<String, SessionInfo> sessionMap;

  public SessionManager() {
    sessionMap = new ConcurrentHashMap<>();
  }

  /**
   * Updates last access time.
   *
   * @param context
   *          Process Context
   * @param timestamp
   *          timestamp of access time
   */
  public synchronized void onAccess(ProcessContext context, long timestamp) {
    cleanInvalidatedSessionInfo(true);

    String sessionId = context.getSessionId();
    if (sessionId == null) {
      return;
    }

    SessionInfo sessionInfo = sessionMap.get(sessionId);
    if (sessionInfo == null) {
      return;
    }

    sessionInfo.updateLastAccessTime(timestamp);

    String remoteAddr = context.getRemoteAddress(true);
    sessionInfo.setRemoteAddr(remoteAddr);

    String ua = context.getUserAgent();
    sessionInfo.setUserAgent(ua);

    String userId = context.getUserId();
    saveSessionInfo(userId);

    try {
      saveTimelineLog(userId, sessionId, timestamp);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Callback for the web application shut down.<br>
   * This is called by AppManager#onStop().
   */
  public void onStop() {
    // Remove invalid info prior to save.
    cleanInvalidatedSessionInfo(false);
    saveAllSessionsInfo();
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
    String userId = info.getUserId();
    AppManager appManager = AppManager.getInstance();
    int max = appManager.getConfigValueAsInteger("max_sessions_per_user", DEFAULT_MAX_SESSIONS_PER_USER);
    int n = max - 1;
    trimSessionInfo(userId, n);

    String sessionId = info.getSessionId();
    sessionMap.put(sessionId, info);
  }

  private void trimSessionInfo(String userId, int n) {
    List<Long> timeList = new ArrayList<>();
    int count = 0;
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo info = sessionMap.get(sessionId);
      String uid = info.getUserId();
      if (uid.equals(userId)) {
        long time = info.getLastAccessTime();
        timeList.add(time);
        count++;
      }
    }

    if (count <= n) {
      return;
    }

    timeList.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo info = sessionMap.get(sessionId);
      String uid = info.getUserId();
      if (uid.equals(userId)) {
        long time = info.getLastAccessTime();
        if (!inListSizeRange(timeList, n, time)) {
          Log.i("Logout: EXCEED_MAX user=" + userId + " sid=" + info.getShortSessionId());
          removeSessionInfo(sessionId);
        }
      }
    }
  }

  private boolean inListSizeRange(List<Long> vList, int n, long v) {
    if (n > vList.size()) {
      n = vList.size();
    }
    for (int i = 0; i < n; i++) {
      if (v == vList.get(i)) {
        return true;
      }
    }
    return false;
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
   * Returns user session count.
   *
   * @param userId
   *          target user id
   * @return the session count for the user
   */
  public int countUserSessions(String userId) {
    int count = 0;
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo info = sessionMap.get(sessionId);
      String uid = info.getUserId();
      if (uid.equals(userId)) {
        count++;
      }
    }
    return count;
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
   * @return removed session info
   */
  public SessionInfo removeSessionInfo(String sessionId) {
    return sessionMap.remove(sessionId);
  }

  /**
   * Removes the session info associated with the user name from the session map.
   *
   * @param userId
   *          target user id
   */
  public void removeSessionInfoByUsername(String userId) {
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      String uid = sessionInfo.getUserId();
      if (uid.equals(userId)) {
        sessionMap.remove(sessionId);
      }
    }
  }

  public void loadAllSessionsInfo() {
    AppManager appManager = AppManager.getInstance();
    UserManager userManager = appManager.getUserManager();
    String[] userIds = userManager.getAllUserIds();
    int sessionCount = 0;
    for (int i = 0; i < userIds.length; i++) {
      String userId = userIds[i];
      int count = loadSessionInfo(userId);
      sessionCount += count;
    }
    Log.i("Total " + sessionCount + " session info loaded");
  }

  /**
   * Loads session info from a file.
   *
   * @param path
   *          Session info file path
   */
  public int loadSessionInfo(String userId) {
    String path = getUserSessionPath(userId);
    String[] records = FileUtil.readTextAsArray(path);
    if (records == null) {
      return 0;
    }

    int count = 0;
    for (int i = 0; i < records.length; i++) {
      String record = records[i];
      if (record.startsWith("#")) {
        continue;
      }
      try {
        restoreSessionInfo(record);
        count++;
      } catch (Exception e) {
        Log.e("Session restore error: userId=" + userId + ": " + e.toString());
      }
    }

    if (count > 0) {
      Log.i(count + " session info loaded: userId=" + userId);
    }

    return count;
  }

  /**
   * Restores the session info to memory
   *
   * @param record
   *          session record
   */
  private void restoreSessionInfo(String record) {
    CsvFieldGetter csvFieldGetter = new CsvFieldGetter(record, "\t");
    String sessionId = csvFieldGetter.getFieldValue();
    String userId = csvFieldGetter.getFieldValue();
    long lastAccessTime = csvFieldGetter.getFieldValueAsLong();
    String remoteAddr = csvFieldGetter.getFieldValue();
    String remoteHost = csvFieldGetter.getFieldValue();
    String userAgent = csvFieldGetter.getFieldValue();
    long loginTime = csvFieldGetter.getFieldValueAsLong();
    String loginRemoteAddr = csvFieldGetter.getFieldValue();
    String loginRemoteHost = csvFieldGetter.getFieldValue();
    String loginUserAgent = csvFieldGetter.getFieldValue();

    SessionInfo info = new SessionInfo(sessionId, userId, lastAccessTime, remoteAddr, remoteHost, userAgent, loginTime, loginRemoteAddr, loginRemoteHost, loginUserAgent);
    registerSessionInfo(info);
  }

  /**
   * Saves all user sessions info into the file.
   */
  private void saveAllSessionsInfo() {
    AppManager appManager = AppManager.getInstance();
    UserManager userManager = appManager.getUserManager();
    String[] userIds = userManager.getAllUserIds();
    saveSessionInfo(userIds);
  }

  /**
   * Saves user sessions info into the file.
   */
  private void saveSessionInfo(String[] userIds) {
    int count = 0;
    for (int i = 0; i < userIds.length; i++) {
      String userId = userIds[i];
      int savedCount = saveSessionInfo(userId);
      count += savedCount;
      if (savedCount > 0) {
        Log.i(savedCount + " session info saved: userId=" + userId);
        Log.i("Total " + count + " session info saved");
      }
    }
  }

  /**
   ** Saves user sessions info into the file.
   *
   * @param userId
   *          target user id
   * @return saved session count
   */
  public int saveSessionInfo(String userId) {
    int count = 0;
    CsvBuilder csvBuilder = new CsvBuilder("\t");
    csvBuilder = buildTsvHeader(csvBuilder);
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo session = sessionMap.get(sessionId);
      String uid = session.getUserId();
      if (!uid.equals(userId)) {
        continue;
      }

      long lastAccessTime = session.getLastAccessTime();
      String remoteAddr = session.getRemoteAddr();
      String remoteHost = session.getRemoteHost();
      String userAgent = session.getUserAgent();
      long createdTime = session.getCreatedTime();
      String createdRemoteAddr = session.getCreatedRemoteAddr();
      String createdRemoteHost = session.getCreatedRemoteHost();
      String createdUserAgent = session.getCreatedUserAgent();

      csvBuilder.append(sessionId);
      csvBuilder.append(userId);
      csvBuilder.append(lastAccessTime);
      csvBuilder.append(remoteAddr);
      csvBuilder.append(remoteHost);
      csvBuilder.append(userAgent);
      csvBuilder.append(createdTime);
      csvBuilder.append(createdRemoteAddr);
      csvBuilder.append(createdRemoteHost);
      csvBuilder.append(createdUserAgent);
      csvBuilder.nextRecord();

      count++;
    }

    String path = getUserSessionPath(userId);

    if (count == 0) {
      FileUtil.delete(path);
      return 0;
    }

    String text = csvBuilder.toString();
    try {
      FileUtil.write(path, text);
    } catch (IOException e) {
      Log.e("Session info save error: userId=" + userId, e);
    }

    return count;
  }

  private CsvBuilder buildTsvHeader(CsvBuilder csvBuilder) {
    csvBuilder.append("#sid");
    csvBuilder.append("uid");
    csvBuilder.append("time");
    csvBuilder.append("addr");
    csvBuilder.append("host");
    csvBuilder.append("ua");
    csvBuilder.append("login_time");
    csvBuilder.append("login_addr");
    csvBuilder.append("login_host");
    csvBuilder.append("login_ua");
    csvBuilder.nextRecord();
    return csvBuilder;
  }

  private String getUserSessionPath(String userId) {
    String userDataPath = UserManager.getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, "sessions.txt");
    return path;
  }

  /**
   * Process on Login.
   *
   * @param context
   *          Process Context
   * @param userId
   *          the user id
   * @return new session object
   */
  public SessionInfo onLoggedIn(ProcessContext context, String userId) {
    SessionInfo currentSession = getSessionInfo(context);

    if (currentSession != null) {
      String sessionId = currentSession.getSessionId();
      Log.i("Logout: RENEW user=" + userId + " sid=" + currentSession.getShortSessionId());
      removeSessionInfo(sessionId);
    }

    SessionInfo sessionInfo = createNewSession(context, userId);
    registerSessionInfo(sessionInfo);

    return sessionInfo;
  }

  /**
   * Creates a new session.
   *
   * @param context
   *          Process Context
   * @param userId
   *          the user id
   * @return new session object
   */
  private SessionInfo createNewSession(ProcessContext context, String userId) {
    // Recreate session
    HttpServletRequest request = context.getRequest();
    HttpSession session = request.getSession();
    session.invalidate();
    session = request.getSession(true);
    long now = System.currentTimeMillis();
    String sessionId = generateSessionId(now, userId);
    String remoteAddr = context.getRemoteAddr();
    String remoteHost = context.getRemoteHost();
    String userAgent = context.getUserAgent();

    SessionInfo sessionInfo = new SessionInfo(sessionId, userId, now, remoteAddr, remoteHost, userAgent, now, remoteAddr, remoteHost, userAgent);

    // Set session expiration
    int sessionTimeoutSec = getSessionTimeout();
    session.setMaxInactiveInterval(sessionTimeoutSec);
    context.setSessionCookieMaxAge(sessionId, sessionTimeoutSec);

    return sessionInfo;
  }

  /**
   * Generate a session ID.
   *
   * @param t
   *          timestamp
   * @param userId
   *          the user id
   * @return Session ID
   */
  private String generateSessionId(long t, String userId) {
    long r = Randomizer.getLong();
    String s = t + userId + r;
    String sessionId = HashUtil.getHashString(s, "SHA-256");
    return sessionId;
  }

  /**
   * Removes expired session info from the management map.
   */
  public void cleanInvalidatedSessionInfo(boolean flush) {
    long now = System.currentTimeMillis();
    AppManager appManager = AppManager.getInstance();
    int sessionTimeoutSec = appManager.getConfigValueAsInteger("session_timeout_sec");
    long timeoutMillis = sessionTimeoutSec * 1000;
    Set<String> clearedUserIds = new LinkedHashSet<>();

    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      long lastAccessTime = sessionInfo.getLastAccessTime();
      long elapsed = now - lastAccessTime;
      if (elapsed > timeoutMillis) {
        String userId = sessionInfo.getUserId();
        Log.i("Logout: EXPIRED user=" + userId + " sid=" + sessionInfo.getShortSessionId());
        sessionMap.remove(sessionId);
        clearedUserIds.add(userId);
      }
    }

    if (flush) {
      flushSessionInfo(clearedUserIds);
    }
  }

  private void flushSessionInfo(Set<String> clearedUserIds) {
    int size = clearedUserIds.size();
    Iterator<String> it = clearedUserIds.iterator();
    String[] userIds = new String[size];
    int i = 0;
    while (it.hasNext()) {
      String userId = it.next();
      userIds[i] = userId;
      i++;
    }
    saveSessionInfo(userIds);
  }

  /**
   * Logout.
   *
   * @param context
   *          Process Context
   */
  public void logout(ProcessContext context) {
    HttpSession httpSession = context.getHttpSession();
    String sessionId = context.getSessionId();
    logout(sessionId);
    httpSession.invalidate();
    invalidateSessionCookie(context);
  }

  /**
   * Logout.
   *
   * @param sessionId
   *          target session id
   * @return true if logged out successfully.
   */
  public boolean logout(String sessionId) {
    String shortSid = getShortSessionId(sessionId);

    SessionInfo sessionInfo = removeSessionInfo(sessionId);
    if (sessionInfo == null) {
      Log.e("Logout: SESION_NOT_FOUND: sid=" + shortSid);
      return false;
    }

    String userId = sessionInfo.getUserId();
    saveSessionInfo(userId);
    cleanInvalidatedSessionInfo(true);
    Log.i("Logout: OK user=" + userId + " sid=" + shortSid);

    int count = countUserSessions(userId);
    if (count == 0) {
      UserManager userManager = UserManager.getInstance();
      UserStatus userStatus = userManager.getUserStatusInfo(userId);
      if (userStatus != null) {
        long timestamp = System.currentTimeMillis();
        userStatus.setLastLogout(timestamp);
        try {
          userManager.saveUserStatus(userId);
        } catch (IOException ioe) {
          Log.e("Write user status error: user=" + userId + ": " + ioe);
        }
      }
    }

    return true;
  }

  /**
   * Clear all user sessions.
   *
   * @param userId
   *          target user id
   */
  public void clearUserSessions(String userId) {
    for (Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
      String sessionId = entry.getKey();
      SessionInfo sessionInfo = sessionMap.get(sessionId);
      String uid = sessionInfo.getUserId();
      if (uid.equals(userId)) {
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

  private String getShortSessionId(String sessionId) {
    return StrUtil.snip(sessionId, 7, 3);
  }

  /**
   * Returns user timeline log.
   *
   * @param userId
   *          target user id
   * @return Log text array
   */
  public String[] getUserTimelineLog(String userId) {
    return loadTimelineLog(userId);
  }

  private String getTimelineLogFilePath(String userId) {
    String userDataPath = UserManager.getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, TIMELINE_LOG_FILE_NAME);
    return path;
  }

  /**
   * Load user timeline log from a storage.
   *
   * @param userId
   *          id target user id
   * @return Log text array
   */
  public String[] loadTimelineLog(String userId) {
    String path = getTimelineLogFilePath(userId);
    String[] logs = FileUtil.readTextAsArray(path);
    if (logs == null) {
      logs = new String[0];
    }
    return logs;
  }

  public void saveTimelineLog(String userId, String sid, long timestamp) throws IOException {
    saveTimelineLog(userId, sid, timestamp, null);
  }

  public void saveTimelineLog(String userId, String sid, long timestamp, String info) throws IOException {
    int TIME_SLOT_MIN = 15;
    int MAX_LOG_LINES = 1000;
    String[] logLines = loadTimelineLog(userId);

    RingBuffer<String> logs = new RingBuffer<>(MAX_LOG_LINES);
    for (int i = 0; i < logLines.length; i++) {
      String line = logLines[i];
      logs.add(line);
    }

    long timeSlotMillis = TIME_SLOT_MIN * 60000;

    List<String> logList = logs.getAll();

    for (int i = logList.size() - 1; i >= 0; i--) {
      String line = logList.get(i);
      String[] wk = line.split("\t");
      long logTime;
      String logSid;
      try {
        logTime = Long.parseLong(wk[0]);
        logSid = wk[1];
      } catch (Exception e) {
        logTime = 0;
        logSid = "";
      }

      if (logSid.equals(sid)) {
        long logTimeSlotSec = (long) (logTime / timeSlotMillis) * timeSlotMillis;
        long elapsedFromLatest = timestamp - logTimeSlotSec;
        if (elapsedFromLatest <= timeSlotMillis) {
          return;
        }
      }
    }

    String newLog = timestamp + "\t" + sid;
    if (info != null) {
      newLog += "\t" + info;
    }
    logs.add(newLog);

    String path = getTimelineLogFilePath(userId);

    logList = logs.getAll();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < logList.size(); i++) {
      String line = logList.get(i);
      sb.append(line);
      sb.append("\n");
    }

    String text = sb.toString();
    FileUtil.write(path, text);
  }

}
