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

import com.libutil.FileUtil;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.auth.Authenticator;

public class UserManager {

  private static final String USERS_FILE_NAME = "users.txt";
  private static final String USERS_PW_FILE_NAME = "userspw.txt";
  private static final String GROUPS_FILE_NAME = "groups.txt";

  private static UserManager instance;

  private Map<String, User> users;
  private Map<String, Group> groups;
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
    loadGroups();
    loadUsers();
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
   * Returns all user info map.
   *
   * @return the map of user info
   */
  public Map<String, User> getAllUserInfo() {
    return users;
  }

  /**
   * Returns the group info of specified group name.
   * 
   * @param groupName
   *          target group name
   * @return the group info
   */
  public Group getGroupInfo(String groupName) {
    return groups.get(groupName);
  }

  /**
   * Returns all group info map.
   *
   * @return the map of group info
   */
  public Map<String, Group> getAllGroupInfo() {
    return groups;
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

    String[] text = FileUtil.readTextAsArray(usersFile);
    for (int i = 0; i < text.length; i++) {
      String line = text[i];
      if (line.startsWith("#")) {
        continue;
      }

      String[] fields = line.split("\t");

      String username = fields[0];

      String fullname = null;
      if (fields.length > 1) {
        fullname = fields[1];
      }

      String localFullName = null;
      if (fields.length > 2) {
        localFullName = fields[2];
      }

      String adminFlag = "";
      boolean isAdmin = false;
      if (fields.length > 3) {
        adminFlag = fields[3];
        isAdmin = "1".equals(adminFlag);
      }

      String groups = null;
      if (fields.length > 4) {
        groups = fields[4];
      }

      String privileges = null;
      if (fields.length > 5) {
        privileges = fields[5];
      }

      int status = User.STATE_NONE;
      if (fields.length > 6) {
        try {
          status = Integer.parseInt(fields[6]);
        } catch (Exception e) {
          // nop
        }
      }

      User user = new User(username, fullname, localFullName, isAdmin, groups, privileges, status);

      if (fields.length > 7) {
        long createdDate = StrUtil.parseLong(fields[7]);
        user.setCreatedDate(createdDate);
      }

      if (fields.length > 8) {
        long updatedDate = StrUtil.parseLong(fields[8]);
        user.setUpdatedDate(updatedDate);
      }

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
    String header = "#Username\tName\tLocalFullName\tisAdmin\tGroups\tPrivileges\tStatus\tCreated\tUpdated\n";
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    for (Entry<String, User> entry : users.entrySet()) {
      User user = entry.getValue();
      String username = user.getUsername();
      String fullname = user.getFullName();
      String localFullName = user.getLocalFullName();
      boolean isAdmin = user.isAdmin();
      String adminFlag = (isAdmin ? "1" : "0");
      String groups = user.getGroupsInOneLine();
      String privileges = user.getPrivilegesInOneLine();
      int status = user.getStatus();
      long createdDate = user.getCreatedDate();
      long updatedDate = user.getUpdatedDate();

      sb.append(username);
      sb.append("\t");
      sb.append(fullname);
      sb.append("\t");
      sb.append(localFullName);
      sb.append("\t");
      sb.append(adminFlag);
      sb.append("\t");
      sb.append(groups);
      sb.append("\t");
      sb.append(privileges);
      sb.append("\t");
      sb.append(status);
      sb.append("\t");
      sb.append(createdDate);
      sb.append("\t");
      sb.append(updatedDate);
      sb.append("\n");
    }

    String dataPath = getDataPath();
    String usersFile = FileUtil.joinPath(dataPath, "users.txt");
    String data = sb.toString();
    try {
      FileUtil.write(usersFile, data);
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param groups
   *          Groups
   * @param privileges
   *          Privileges
   * @param userStatus
   *          user status
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User regieterNewUser(String username, String pwHash, String fullname, String localFullName, String adminFlag, String groups, String privileges, String userStatus) throws Exception {
    if (users.containsKey(username)) {
      throw new Exception("USER_ALREADY_EXISTS");
    }

    int ret = authenticator.registerByHash(username, pwHash);
    if (ret < 0) {
      throw new Exception("PW_REGISTER_ERROR");
    }

    boolean isAdmin = "1".equals(adminFlag);
    int status = StrUtil.parseInt(userStatus, User.STATE_NONE);

    long createdDate = System.currentTimeMillis();
    long updatedDate = createdDate;

    User user = new User(username, fullname, localFullName, isAdmin, groups, privileges, status, createdDate, updatedDate);
    user.setState(User.STATE_NEED_PW_CHANGE);
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param groups
   *          Groups
   * @param privileges
   *          Privileges
   * @param userStatus
   *          user status
   * @return User
   * @throws Exception
   *           if an error occurres
   */
  public User updateUser(String username, String pwHash, String fullname, String localFullName, String adminFlag, String groups, String privileges, String userStatus) throws Exception {
    User user = users.get(username);
    if (user == null) {
      throw new Exception("USER_NOT_FOUND");
    }

    if (adminFlag != null) {
      boolean isAdmin = "1".equals(adminFlag);
      user.setAdmin(isAdmin);
    }

    if (fullname != null) {
      user.setFullName(fullname);
    }

    if (localFullName != null) {
      user.setLocalFullName(localFullName);
    }

    if (groups != null) {
      user.setGroups(groups);
    }

    if (privileges != null) {
      user.setPrivileges(privileges);
    }

    if (userStatus != null) {
      int status = StrUtil.parseInt(userStatus, User.STATE_NONE);
      user.setStatus(status);
    }

    long updatedDate = System.currentTimeMillis();
    user.setUpdatedDate(updatedDate);

    if (pwHash != null) {
      int ret = authenticator.registerByHash(username, pwHash);
      if (ret < 0) {
        throw new Exception("PW_REGISTER_ERROR");
      }
      user.unsetState(User.STATE_NEED_PW_CHANGE);
    }

    try {
      saveUsers();
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

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_DELETE");
    }
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
   * Load user info from a storage.
   */
  public void loadGroups() {
    groups = new LinkedHashMap<>();
    String dataPath = getDataPath();
    String groupsFile = FileUtil.joinPath(dataPath, GROUPS_FILE_NAME);

    String[] text = FileUtil.readTextAsArray(groupsFile);
    if (text == null) {
      // group definition file not found
      return;
    }

    for (int i = 0; i < text.length; i++) {
      String line = text[i];
      if (line.startsWith("#")) {
        continue;
      }

      String[] fields = line.split("\t");

      String groupName = fields[0];

      String privileges = null;
      if (fields.length > 1) {
        privileges = fields[1];
      }

      Group group = new Group(groupName, privileges);
      groups.put(groupName, group);
    }
  }

  private String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

}
