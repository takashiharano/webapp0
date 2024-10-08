/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.libutil.CsvBuilder;
import com.libutil.FileUtil;
import com.libutil.Props;
import com.libutil.StrUtil;
import com.libutil.auth.Authenticator;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.CsvFieldGetter;
import com.takashiharano.webapp0.util.Log;

public class UserManager {

  private static final String USERS_FILE_NAME = "users.txt";
  private static final String USERS_PW_FILE_NAME = "userspw.txt";
  private static final String USER_DATA_ROOT_DIR = "users";
  private static final String USER_STATUS_FILE_NAME = "status.txt";
  private static final String USER_MEMO_FILE_NAME = "memo.txt";

  private static UserManager instance;

  private Map<String, User> users;
  private Authenticator authenticator;

  /**
   * Initializes the manager object and loads users info from a storage.
   */
  public UserManager() {
    init();
  }

  public static UserManager getInstance() {
    if (instance == null) {
      instance = new UserManager();
    }
    return instance;
  }

  public static String getUserDataPath(String userId) {
    String rootPath = getUserDataRootPath();
    String path = FileUtil.joinPath(rootPath, userId);
    return path;
  }

  public void init() {
    loadUsers();
  }

  /**
   * Updates last access time in user status info.
   *
   * @param context
   *          Process Context
   * @param timestamp
   *          the timestamp of the current date-time
   */
  public synchronized void onAccess(ProcessContext context, long timestamp) {
    String userId = context.getUserId();
    if (userId == null) {
      return;
    }

    UserStatus userStatus = getUserStatusInfo(userId);
    if (userStatus == null) {
      return;
    }

    userStatus.setLastAccess(timestamp);

    try {
      saveUserStatus(userId, userStatus);
    } catch (IOException ioe) {
      Log.e("onAccess(): Write user status error: user=" + userId + ": " + ioe);
    }
  }

  /**
   * Callback for the web application shut down.<br>
   * This is called by AppManager#onStop().
   */
  public void onStop() {
    try {
      saveAllUserStatus();
    } catch (Exception e) {
      Log.e(e);
    }
  }

  /**
   * Returns all user info map.
   *
   * @return the map of user info
   */
  public Map<String, User> getAllUserInfo() {
    return users;
  }

  /**
   * Returns all user IDs.
   *
   * @return all user IDs
   */
  public String[] getAllUserIds() {
    int size = users.size();
    String[] ids = new String[size];
    int i = 0;
    for (Entry<String, User> entry : users.entrySet()) {
      String userId = entry.getKey();
      ids[i] = userId;
      i++;
    }
    return ids;
  }

  /**
   * Returns the user info of specified user id.
   * 
   * @param userId
   *          target user id
   * @return the user info
   */
  public User getUserInfo(String userId) {
    return users.get(userId);
  }

  /**
   * Authenticate the user.
   *
   * @param user
   *          id the target user id
   * @param pwHash
   *          the password hash for the user.<br>
   *          the hash is sha256(plain-pw + user_id).<br>
   *          it must be lower case.
   * @return The result status string. "OK" or any error status.
   */
  public String authenticate(ProcessContext context, String userId, String pwHash) throws Exception {
    if (StrUtil.isBlank(userId) || StrUtil.isBlank(pwHash)) {
      return "EMPTY_VALUE";
    }

    User user = getUserInfo(userId);
    if (user == null) {
      return "USER_NOT_FOUND";
    }

    if (user.hasFlag(User.FLAG_DISABLED)) {
      return "DISABLED";
    }

    if (isLocked(context, userId)) {
      return "LOCKED";
    }

    String status;
    AppManager appManager = AppManager.getInstance();
    String authControl = appManager.getConfigValue("auth_control");
    if ("pseudo".equals(authControl)) {
      status = "OK";
    } else {
      status = authenticator.auth(userId, pwHash);
    }

    return status;
  }

  private boolean isLocked(ProcessContext context, String userId) throws Exception {
    int loginFailedCount = getLoginFailedCount(userId);
    long loginLockedTime = getLoginFailedTime(userId);
    int loginFailureMaxCount = context.getConfigValueAsInteger("login_failure_max");
    long loginLockPeriodMillis = context.getConfigValueAsInteger("login_lock_period_sec") * 1000;

    if ((loginFailureMaxCount > 0) && (loginFailedCount >= loginFailureMaxCount)) {
      long now = System.currentTimeMillis();
      long elapsed = now - loginLockedTime;
      if ((loginLockPeriodMillis == 0) || (elapsed <= loginLockPeriodMillis)) {
        return true;
      } else {
        resetLoginFailedCount(userId);
      }
    }

    return false;
  }

  /**
   * Load user info from a storage.
   */
  public void loadUsers() {
    users = new LinkedHashMap<>();
    String dataPath = getDataPath();
    String usersFile = FileUtil.joinPath(dataPath, USERS_FILE_NAME);
    String passFile = FileUtil.joinPath(dataPath, USERS_PW_FILE_NAME);
    authenticator = new Authenticator(passFile, 1);

    String[] lines = FileUtil.readTextAsArray(usersFile);
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.startsWith("#")) {
        continue;
      }

      CsvFieldGetter csvFieldGetter = new CsvFieldGetter(line, "\t");
      String userId = csvFieldGetter.getFieldValue();
      String fullname = csvFieldGetter.getFieldValue();
      String localFullName = csvFieldGetter.getFieldValue();
      String aliaslName = csvFieldGetter.getFieldValue();
      String email = csvFieldGetter.getFieldValue();
      boolean isAdmin = csvFieldGetter.getFieldValueAsBoolean("1");
      String groups = csvFieldGetter.getFieldValue();
      String privileges = csvFieldGetter.getFieldValue();
      String info1 = csvFieldGetter.getFieldValue();
      String info2 = csvFieldGetter.getFieldValue();
      String info3 = csvFieldGetter.getFieldValue();
      int status = csvFieldGetter.getFieldValueAsInteger(User.FLAG_NEED_PW_CHANGE);
      long createdAt = csvFieldGetter.getFieldValueAsLong();
      long updatedAt = csvFieldGetter.getFieldValueAsLong();
      String memo = loadUserMemo(userId);

      User user = new User(userId, fullname, localFullName, aliaslName, email, isAdmin, groups, privileges, info1, info2, info3, memo, status);
      user.setCreatedAt(createdAt);
      user.setUpdatedAt(updatedAt);

      UserStatus userStatus = loadUserStatus(userId);
      user.setUserStatus(userStatus);

      users.put(userId, user);
    }
  }

  /**
   * Write user info into a storage.
   *
   * @throws IOException
   *           if an IO error occurres
   */
  public synchronized void saveUsers() throws IOException {
    CsvBuilder csvBuilder = new CsvBuilder("\t", false);
    csvBuilder.setLineBreak("\n");
    csvBuilder = buildTsvHeader(csvBuilder);

    for (Entry<String, User> entry : users.entrySet()) {
      User user = entry.getValue();
      String userId = user.getUserId();
      String fullname = user.getFullName();
      String localFullName = user.getLocalFullName();
      String aliasName = user.getAliasName();
      String email = user.getEmail();
      boolean isAdmin = user.isAdmin();
      String adminFlag = (isAdmin ? "1" : "0");
      String groups = user.getGroupsInOneLine();
      String privileges = user.getPrivilegesInOneLine();
      String info1 = user.getInfo1();
      String info2 = user.getInfo2();
      String info3 = user.getInfo3();
      String memo = user.getMemo();
      int flags = user.getFlags();
      long createdAt = user.getCreatedAt();
      long updatedAt = user.getUpdatedAt();

      csvBuilder.append(userId);
      csvBuilder.append(fullname);
      csvBuilder.append(localFullName);
      csvBuilder.append(aliasName);
      csvBuilder.append(email);
      csvBuilder.append(adminFlag);
      csvBuilder.append(groups);
      csvBuilder.append(privileges);
      csvBuilder.append(info1);
      csvBuilder.append(info2);
      csvBuilder.append(info3);
      csvBuilder.append(flags);
      csvBuilder.append(createdAt);
      csvBuilder.append(updatedAt);
      csvBuilder.nextRecord();

      saveUserMemo(userId, memo);
    }

    String dataPath = getDataPath();
    String usersFilePath = FileUtil.joinPath(dataPath, USERS_FILE_NAME);
    String data = csvBuilder.toString();
    try {
      FileUtil.write(usersFilePath, data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  private CsvBuilder buildTsvHeader(CsvBuilder csvBuilder) {
    csvBuilder.append("#uid");
    csvBuilder.append("fullname");
    csvBuilder.append("localFullName");
    csvBuilder.append("aliasName");
    csvBuilder.append("email");
    csvBuilder.append("isAdmin");
    csvBuilder.append("groups");
    csvBuilder.append("privileges");
    csvBuilder.append("info1");
    csvBuilder.append("info2");
    csvBuilder.append("info3");
    csvBuilder.append("flags");
    csvBuilder.append("created_at");
    csvBuilder.append("updated_at");
    csvBuilder.nextRecord();
    return csvBuilder;
  }

  /**
   * Register new user.
   *
   * @param userId
   *          User id
   * @param pwHash
   *          Password hash
   * @param fullname
   *          Full name
   * @param localFullName
   *          Local full name
   * @param aliasName
   *          Alias name
   * @param email
   *          Email address
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param groups
   *          Groups
   * @param privileges
   *          Privileges
   * @param info1
   *          info1
   * @param info2
   *          info2
   * @param info3
   *          info3
   * @param memo
   *          user memo
   * @param userFlags
   *          user flags
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User regieterNewUser(String userId, String pwHash, String fullname, String localFullName, String aliasName, String email, String adminFlag, String groups, String privileges, String info1, String info2, String info3, String memo, String userFlags) throws Exception {
    if (users.containsKey(userId)) {
      throw new Exception("USER_ALREADY_EXISTS");
    }

    int ret = authenticator.registerByHash(userId, pwHash);
    if (ret < 0) {
      throw new Exception("PW_REGISTER_ERROR");
    }

    boolean isAdmin = "1".equals(adminFlag);
    int flags = StrUtil.parseInt(userFlags, User.FLAG_NONE);

    long now = System.currentTimeMillis();
    long createdDate = now;
    long updatedDate = now;

    User user = new User(userId, fullname, localFullName, aliasName, email, isAdmin, groups, privileges, info1, info2, info3, memo, flags, createdDate, updatedDate);
    if (StrUtil.isEmpty(userFlags)) {
      user.setFlag(User.FLAG_NEED_PW_CHANGE);
    }
    users.put(userId, user);

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_REGISTER");
    }

    return user;
  }

  /**
   * Update a user.
   *
   * @param userId
   *          User id
   * @param pwHash
   *          Password hash
   * @param fullname
   *          Full name
   * @param localFullName
   *          Local full name
   * @param aliasName
   *          Alias name
   * @param email
   *          Email address
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param groups
   *          Groups
   * @param privileges
   *          Privileges
   * @param info1
   *          info1
   * @param info2
   *          info2
   * @param info3
   *          info3
   * @param memo
   *          user memo
   * @param userFlags
   *          user flags
   * @param onlyChangePass
   *          Set true if only changing password
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User updateUser(String userId, String pwHash, String fullname, String localFullName, String aliasName, String email, String adminFlag, String groups, String privileges, String info1, String info2, String info3, String memo, String userFlags, boolean onlyChangePass) throws Exception {
    User user = users.get(userId);
    if (user == null) {
      throw new Exception("USER_NOT_FOUND");
    }

    boolean updated = false;

    if (adminFlag != null) {
      boolean isAdmin = "1".equals(adminFlag);
      user.setAdmin(isAdmin);
      updated = true;
    }

    if (fullname != null) {
      user.setFullName(fullname);
      updated = true;
    }

    if (localFullName != null) {
      user.setLocalFullName(localFullName);
      updated = true;
    }

    if (aliasName != null) {
      user.setAliasName(aliasName);
      updated = true;
    }

    if (email != null) {
      user.setEmail(email);
      updated = true;
    }

    if (groups != null) {
      user.setGroups(groups);
      updated = true;
    }

    if (privileges != null) {
      user.setPrivileges(privileges);
      updated = true;
    }

    if (info1 != null) {
      user.setInfo1(info1);
      updated = true;
    }

    if (info2 != null) {
      user.setInfo2(info2);
      updated = true;
    }

    if (info3 != null) {
      user.setInfo3(info3);
      updated = true;
    }

    if (memo != null) {
      user.setMemo(memo);
      updated = true;
    }

    if (userFlags != null) {
      int flags = StrUtil.parseInt(userFlags, User.FLAG_NONE);
      user.setFlags(flags);
      updated = true;
    }

    long now = System.currentTimeMillis();
    if (updated) {
      user.setUpdatedAt(now);
    }

    UserStatus userStatus = getUserStatusInfo(userId);
    if (pwHash != null) {
      int ret = authenticator.registerByHash(userId, pwHash);
      if (ret < 0) {
        throw new Exception("PW_REGISTER_ERROR");
      }
      if (onlyChangePass) {
        user.unsetFlag(User.FLAG_NEED_PW_CHANGE);
      }
      userStatus.setPwChangedTime(now);
    }

    try {
      saveUsers();
      saveUserStatus(userId, userStatus);
      if (memo != null) {
        saveUserMemo(userId, memo);
      }
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_UPDATE");
    }

    return user;
  }

  /**
   * Changes the user password.
   *
   * @param userId
   *          Target user id
   * @param pwHash
   *          Password hash
   * @throws Exception
   *           if an error occurres
   */
  public void changePassword(String userId, String pwHash) throws Exception {
    updateUser(userId, pwHash, null, null, null, null, null, null, null, null, null, null, null, null, true);
  }

  /**
   * Deletes a user
   *
   * @param userId
   *          userId
   * @throws Exception
   *           if an error occures
   */
  public void deleteUser(String userId) throws Exception {
    User user = users.get(userId);
    if (user == null) {
      throw new Exception("USER_NOT_FOUND");
    }

    authenticator.remove(userId);
    users.remove(userId);
    deleteUserDataDir(userId);

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_DELETE");
    }
  }

  public void unlockUser(String userId) throws Exception {
    resetLoginFailedCount(userId);
  }

  /**
   * Returns if the specified user exists.
   *
   * @param userId
   *          the user id for search
   * @return true if the user exists; otherwise false
   */
  public boolean existsUser(String userId) {
    return users.containsKey(userId);
  }

  /**
   * Load user memo.
   *
   * @param userId
   *          target user id
   * @return the memo text
   */
  public String loadUserMemo(String userId) {
    String userDataPath = getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, USER_MEMO_FILE_NAME);
    String memo = FileUtil.readText(path);
    if (memo == null) {
      memo = "";
    }
    return memo;
  }

  /**
   * Write user memo into a storage.
   *
   * @param userId
   *          target user id
   * @param memo
   *          the memo to save
   * @throws IOException
   *           if an IO error occurres
   */
  public void saveUserMemo(String userId, String memo) throws IOException {
    String userDataPath = getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, USER_MEMO_FILE_NAME);
    try {
      if ("".equals(memo)) {
        FileUtil.delete(path);
      } else {
        FileUtil.write(path, memo);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  /**
   * Load user status info from a storage.
   *
   * @param userId
   *          target user id
   * @return UserStatus object
   */
  public UserStatus loadUserStatus(String userId) {
    String userDataPath = getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, USER_STATUS_FILE_NAME);
    UserStatus userStatus;
    try {
      Props props = new Props(path);
      long lastAccess = props.getValueAsLong("last_access");
      long pwChangedTime = props.getValueAsLong("pw_changed_at");
      int loginFailedCount = props.getValueAsInteger("login_failed_count");
      long loginFailedTime = props.getValueAsLong("login_failed_time");
      userStatus = new UserStatus(userId, lastAccess, pwChangedTime, loginFailedCount, loginFailedTime);

      long lastLogin = props.getValueAsLong("last_login");
      long lastLogout = props.getValueAsLong("last_logout");
      userStatus.setLastLogin(lastLogin);
      userStatus.setLastLogout(lastLogout);
    } catch (Exception e) {
      userStatus = new UserStatus(userId);
    }
    return userStatus;
  }

  /**
   * Write all user status info into a storage.
   *
   * @throws IOException
   *           if an IO error occurres
   */
  public void saveAllUserStatus() throws IOException {
    for (Entry<String, User> entry : users.entrySet()) {
      String userId = entry.getKey();
      User user = entry.getValue();
      UserStatus userStatus = user.getUserStatus();
      saveUserStatus(userId, userStatus);
    }
  }

  public void saveUserStatus(String userId) throws IOException {
    User user = users.get(userId);
    UserStatus userStatus = user.getUserStatus();
    saveUserStatus(userId, userStatus);
  }

  public void saveUserStatus(String userId, UserStatus userStatus) throws IOException {
    long lastAccess = userStatus.getLastAccess();
    long lastLogin = userStatus.getLastLogin();
    long lastLogout = userStatus.getLastLogout();
    long pwChangedTime = userStatus.getPwChangedTime();
    int loginFailedCount = userStatus.getLoginFailedCount();
    long loginFailedTime = userStatus.getLoginFailedTime();

    StringBuilder sb = new StringBuilder();
    sb.append("last_access=");
    sb.append(lastAccess);
    sb.append("\n");

    sb.append("last_login=");
    sb.append(lastLogin);
    sb.append("\n");

    sb.append("last_logout=");
    sb.append(lastLogout);
    sb.append("\n");

    sb.append("pw_changed_at=");
    sb.append(pwChangedTime);
    sb.append("\n");

    sb.append("login_failed_count=");
    sb.append(loginFailedCount);
    sb.append("\n");

    sb.append("login_failed_time=");
    sb.append(loginFailedTime);
    sb.append("\n");

    String data = sb.toString();

    String userDataPath = getUserDataPath(userId);
    String path = FileUtil.joinPath(userDataPath, USER_STATUS_FILE_NAME);
    try {
      FileUtil.write(path, data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  /**
   * Returns the user status info of specified user id.
   * 
   * @param userId
   *          target user id
   * @return the user status info
   */
  public UserStatus getUserStatusInfo(String userId) {
    UserStatus status = null;
    User user = users.get(userId);
    if (user != null) {
      status = user.getUserStatus();
    }
    return status;
  }

  public int getLoginFailedCount(String userId) {
    UserStatus userStatus = getUserStatusInfo(userId);
    if (userStatus == null) {
      return -1;
    }
    return userStatus.getLoginFailedCount();
  }

  public long getLoginFailedTime(String userId) {
    UserStatus userStatus = getUserStatusInfo(userId);
    if (userStatus == null) {
      return -1;
    }
    return userStatus.getLoginFailedTime();
  }

  public void setLoginFailedCount(String userId, boolean reset) throws Exception {
    UserStatus userStatus = getUserStatusInfo(userId);
    if (userStatus == null) {
      throw new Exception("USER_STATUS_INFO_NOT_FOUND: user=" + userId);
    }

    long timestamp = 0;
    int count = 0;
    if (!reset) {
      timestamp = System.currentTimeMillis();
      count = userStatus.getLoginFailedCount() + 1;
    }

    userStatus.setLoginFailedCount(count);
    userStatus.setLoginFailedTime(timestamp);

    saveUserStatus(userId, userStatus);
  }

  public void incrementLoginFailedCount(String userId) throws Exception {
    setLoginFailedCount(userId, false);
  }

  public void resetLoginFailedCount(String userId) throws Exception {
    setLoginFailedCount(userId, true);
  }

  private static String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

  private static String getUserDataRootPath() {
    String dataPath = getDataPath();
    String path = FileUtil.joinPath(dataPath, USER_DATA_ROOT_DIR);
    return path;
  }

  private void deleteUserDataDir(String userId) {
    String path = getUserDataPath(userId);
    FileUtil.delete(path, true);
  }

}
