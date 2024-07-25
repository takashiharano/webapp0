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

  public void init() {
    loadUsers();
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
   * Updates last access time in user status info.
   *
   * @param context
   *          Process Context
   * @param timestamp
   *          the timestamp of the current date-time
   */
  public void onAccess(ProcessContext context, long timestamp) {
    String username = context.getUsername();
    if (username == null) {
      return;
    }
    UserStatus userStatus = getUserStatusInfo(username);
    if (userStatus == null) {
      return;
    }
    userStatus.setLastAccess(timestamp);
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
   * Returns the user info of specified username.
   * 
   * @param username
   *          target username
   * @return the user info
   */
  public User getUserInfo(String username) {
    return users.get(username);
  }

  /**
   * Authenticate the user.
   *
   * @param username
   *          the target username
   * @param pwHash
   *          the password hash for the user.<br>
   *          the hash is sha256(plain-pw + username).<br>
   *          it must be lower case.
   * @return The result status string. "OK" or any error status.
   */
  public String authenticate(String username, String pwHash) {
    if (StrUtil.isBlank(username) || StrUtil.isBlank(pwHash)) {
      return "EMPTY_VALUE";
    }

    User user = getUserInfo(username);
    if (user == null) {
      return "USER_NOT_FOUND";
    }

    AppManager appManager = AppManager.getInstance();

    String status;
    String authControl = appManager.getConfigValue("auth_control");
    if ("pseudo".equals(authControl)) {
      status = "OK";
    } else {
      status = authenticator.auth(username, pwHash);
    }

    return status;
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

      CsvFieldGetter csvFieldGetter = new CsvFieldGetter(line);
      String username = csvFieldGetter.getFieldValue();
      String fullname = csvFieldGetter.getFieldValue();
      String localFullName = csvFieldGetter.getFieldValue();
      String email = csvFieldGetter.getFieldValue();
      boolean isAdmin = csvFieldGetter.getFieldValueAsBoolean("1");
      String groups = csvFieldGetter.getFieldValue();
      String privileges = csvFieldGetter.getFieldValue();
      String info1 = csvFieldGetter.getFieldValue();
      String info2 = csvFieldGetter.getFieldValue();
      String description = csvFieldGetter.getFieldValue();
      int status = csvFieldGetter.getFieldValueAsInteger(User.FLAG_NEED_PW_CHANGE);
      long createdDate = csvFieldGetter.getFieldValueAsLong();
      long updatedDate = csvFieldGetter.getFieldValueAsLong();

      User user = new User(username, fullname, localFullName, email, isAdmin, groups, privileges, info1, info2, description, status);
      user.setCreatedDate(createdDate);
      user.setUpdatedDate(updatedDate);

      UserStatus userStatus = loadUserStatus(username);
      user.setUserStatus(userStatus);

      users.put(username, user);
    }
  }

  /**
   * Write user info into a storage.
   *
   * @throws IOException
   *           if an IO error occurres
   */
  public void saveUsers() throws IOException {
    String header = "#Username\tName\tLocalFullName\tisAdmin\tGroups\tPrivileges\tDescription\tFlags\tCreated\tUpdated\n";

    CsvBuilder csvBuilder = new CsvBuilder("\t", false);
    csvBuilder.setLineBreak("\n");
    csvBuilder.appendAsIs(header);

    for (Entry<String, User> entry : users.entrySet()) {
      User user = entry.getValue();
      String username = user.getUsername();
      String fullname = user.getFullName();
      String localFullName = user.getLocalFullName();
      String email = user.getEmail();
      boolean isAdmin = user.isAdmin();
      String adminFlag = (isAdmin ? "1" : "0");
      String groups = user.getGroupsInOneLine();
      String privileges = user.getPrivilegesInOneLine();
      String info1 = user.getInfo1();
      String info2 = user.getInfo2();
      String description = user.getDescription();
      int flags = user.getFlags();
      long createdDate = user.getCreatedDate();
      long updatedDate = user.getUpdatedDate();

      csvBuilder.append(username);
      csvBuilder.append(fullname);
      csvBuilder.append(localFullName);
      csvBuilder.append(email);
      csvBuilder.append(adminFlag);
      csvBuilder.append(groups);
      csvBuilder.append(privileges);
      csvBuilder.append(info1);
      csvBuilder.append(info2);
      csvBuilder.append(description);
      csvBuilder.append(flags);
      csvBuilder.append(createdDate);
      csvBuilder.append(updatedDate);
      csvBuilder.nextRecord();
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

  /**
   * Register new user.
   *
   * @param username
   *          Username
   * @param pwHash
   *          Password hash
   * @param fullname
   *          Full name
   * @param localFullName
   *          Local full name
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
   * @param description
   *          user description
   * @param userFlags
   *          user flags
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User regieterNewUser(String username, String pwHash, String fullname, String localFullName, String email, String adminFlag, String groups, String privileges, String info1, String info2, String description, String userFlags) throws Exception {
    if (users.containsKey(username)) {
      throw new Exception("USER_ALREADY_EXISTS");
    }

    int ret = authenticator.registerByHash(username, pwHash);
    if (ret < 0) {
      throw new Exception("PW_REGISTER_ERROR");
    }

    boolean isAdmin = "1".equals(adminFlag);
    int flags = StrUtil.parseInt(userFlags, User.FLAG_NONE);

    long now = System.currentTimeMillis();
    long createdDate = now;
    long updatedDate = now;

    User user = new User(username, fullname, localFullName, email, isAdmin, groups, privileges, info1, info2, description, flags, createdDate, updatedDate);
    if (StrUtil.isEmpty(userFlags)) {
      user.setFlag(User.FLAG_NEED_PW_CHANGE);
    }
    users.put(username, user);

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
   * @param username
   *          Username
   * @param pwHash
   *          Password hash
   * @param fullname
   *          Full name
   * @param localFullName
   *          Local full name
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
   * @param description
   *          user description
   * @param userFlags
   *          user flags
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User updateUser(String username, String pwHash, String fullname, String localFullName, String email, String adminFlag, String groups, String privileges, String info1, String info2, String description, String userFlags) throws Exception {
    User user = users.get(username);
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

    if (description != null) {
      user.setDescription(description);
      updated = true;
    }

    if (userFlags != null) {
      int flags = StrUtil.parseInt(userFlags, User.FLAG_NONE);
      user.setFlags(flags);
      updated = true;
    }

    long now = System.currentTimeMillis();
    if (updated) {
      user.setUpdatedDate(now);
    }

    UserStatus userStatus = getUserStatusInfo(username);
    if (pwHash != null) {
      int ret = authenticator.registerByHash(username, pwHash);
      if (ret < 0) {
        throw new Exception("PW_REGISTER_ERROR");
      }
      user.unsetFlag(User.FLAG_NEED_PW_CHANGE);
      userStatus.setPwChangedTime(now);
    }

    try {
      saveUsers();
      saveUserStatus(username, userStatus);
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_UPDATE");
    }

    return user;
  }

  /**
   * Deletes a user
   *
   * @param username
   *          Username
   * @throws Exception
   *           if an error occures
   */
  public void deleteUser(String username) throws Exception {
    User user = users.get(username);
    if (user == null) {
      throw new Exception("USER_NOT_FOUND");
    }

    authenticator.remove(username);
    users.remove(username);
    deleteUserDataDir(username);

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_DELETE");
    }
  }

  public void unlockUser(String username) throws Exception {
    resetLoginFailedCount(username);
  }

  /**
   * Returns if the specified user exists.
   *
   * @param username
   *          the username for search
   * @return true if the user exists; otherwise false
   */
  public boolean existsUser(String username) {
    return users.containsKey(username);
  }

  /**
   * Load user status info from a storage.
   *
   * @param username
   *          target username
   * @return UserStatus object
   */
  public UserStatus loadUserStatus(String username) {
    String userDataPath = getUserDataPath(username);
    String path = FileUtil.joinPath(userDataPath, USER_STATUS_FILE_NAME);
    UserStatus userStatus;
    try {
      Props props = new Props(path);
      long lastAccess = props.getValueAsLong("last_access");
      long pwChangedTime = props.getValueAsLong("pw_changed_at");
      int loginFailedCount = props.getValueAsInteger("login_failed_count");
      long loginFailedTime = props.getValueAsLong("login_failed_time");
      userStatus = new UserStatus(username, lastAccess, pwChangedTime, loginFailedCount, loginFailedTime);

      long lastLogin = props.getValueAsLong("last_login");
      long lastLogout = props.getValueAsLong("last_logout");
      userStatus.setLastLogin(lastLogin);
      userStatus.setLastLogout(lastLogout);
    } catch (Exception e) {
      userStatus = new UserStatus(username);
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
      String username = entry.getKey();
      User user = entry.getValue();
      UserStatus userStatus = user.getUserStatus();
      saveUserStatus(username, userStatus);
    }
  }

  public void saveUserStatus(String username) throws IOException {
    User user = users.get(username);
    UserStatus userStatus = user.getUserStatus();
    saveUserStatus(username, userStatus);
  }

  public void saveUserStatus(String username, UserStatus userStatus) throws IOException {
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

    String userDataPath = getUserDataPath(username);
    String path = FileUtil.joinPath(userDataPath, USER_STATUS_FILE_NAME);
    try {
      FileUtil.write(path, data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  /**
   * Returns the user status info of specified username.
   * 
   * @param username
   *          target username
   * @return the user status info
   */
  public UserStatus getUserStatusInfo(String username) {
    UserStatus status = null;
    User user = users.get(username);
    if (user != null) {
      status = user.getUserStatus();
    }
    return status;
  }

  public int getLoginFailedCount(String username) {
    UserStatus userStatus = getUserStatusInfo(username);
    if (userStatus == null) {
      return -1;
    }
    return userStatus.getLoginFailedCount();
  }

  public long getLoginFailedTime(String username) {
    UserStatus userStatus = getUserStatusInfo(username);
    if (userStatus == null) {
      return -1;
    }
    return userStatus.getLoginFailedTime();
  }

  public void setLoginFailedCount(String username, boolean reset) throws Exception {
    UserStatus userStatus = getUserStatusInfo(username);
    if (userStatus == null) {
      throw new Exception("USER_STATUS_INFO_NOT_FOUND: user=" + username);
    }

    long timestamp = 0;
    int count = 0;
    if (!reset) {
      timestamp = System.currentTimeMillis();
      count = userStatus.getLoginFailedCount() + 1;
    }

    userStatus.setLoginFailedCount(count);
    userStatus.setLoginFailedTime(timestamp);

    saveUserStatus(username, userStatus);
  }

  public void incrementLoginFailedCount(String username) throws Exception {
    setLoginFailedCount(username, false);
  }

  public void resetLoginFailedCount(String username) throws Exception {
    setLoginFailedCount(username, true);
  }

  private String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

  private String getUserDataRootPath() {
    String dataPath = getDataPath();
    String path = FileUtil.joinPath(dataPath, USER_DATA_ROOT_DIR);
    return path;
  }

  private String getUserDataPath(String username) {
    String rootPath = getUserDataRootPath();
    String path = FileUtil.joinPath(rootPath, username);
    return path;
  }

  private void deleteUserDataDir(String username) {
    String path = getUserDataPath(username);
    FileUtil.delete(path, true);
  }

}
