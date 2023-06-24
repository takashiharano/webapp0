/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.user;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.libutil.FileUtil;
import com.libutil.HashUtil;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.auth.Authenticator;

public class UserManager {

  private static final String USERS_FILE_NAME = "users.txt";
  private static final String USERS_PW_FILE_NAME = "userspw.txt";

  private Map<String, UserInfo> users;
  private Authenticator authenticator;

  public UserManager() {
    loadUsers();
  }

  public UserInfo getUserInfo(String username) {
    return users.get(username);
  }

  public Map<String, UserInfo> getAllUserInfo() {
    return users;
  }

  public UserInfo[] getUserInfoList() {
    UserInfo[] list = new UserInfo[users.size()];
    int i = 0;
    for (Entry<String, UserInfo> entry : users.entrySet()) {
      UserInfo info = entry.getValue();
      list[i] = info;
      i++;
    }
    return list;
  }

  public String authenticate(String username, String pass) {
    if (StrUtil.isBlank(username) || StrUtil.isBlank(pass)) {
      return "EMPTY_VALUE";
    }

    UserInfo user = getUserInfo(username);
    if (user == null) {
      return "USER_NOT_FOUND";
    }

    AppManager appManager = AppManager.getInstance();

    String result;
    String authControl = appManager.getConfigValue("auth_control");
    if ("pseudo".equals(authControl)) {
      result = "OK";
    } else {
      String pwHash = HashUtil.getHashString(pass + username, "SHA-256");
      result = authenticator.auth(username, pwHash);
    }

    return result;
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

      String adminFlag = "";
      boolean isAdmin = false;
      if (fields.length > 2) {
        adminFlag = fields[2];
        isAdmin = "1".equals(adminFlag);
      }

      String privileges = null;
      if (fields.length > 3) {
        privileges = fields[3];
      }

      int status = UserInfo.STATE_NONE;
      if (fields.length > 4) {
        try {
          status = Integer.parseInt(fields[4]);
        } catch (Exception e) {
          // nop
        }
      }

      UserInfo userInfo = new UserInfo(username, fullname, isAdmin, privileges, status);
      users.put(username, userInfo);
    }
  }

  /**
   * Write user info into a storage.
   *
   * @throws IOException
   *           if an IO error occurres
   */
  public void saveUsers() throws IOException {
    String header = "#Username\tName\tisAdmin\tPrivileges\tStatus\n";
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    for (Entry<String, UserInfo> entry : users.entrySet()) {
      UserInfo user = entry.getValue();
      String username = user.getUsername();
      String fullname = user.getFullName();
      boolean isAdmin = user.isAdmin();
      String privileges = user.getPrivilegesInOneLine();
      int status = user.getStatus();
      String adminFlag = (isAdmin ? "1" : "0");
      sb.append(username);
      sb.append("\t");
      sb.append(fullname);
      sb.append("\t");
      sb.append(adminFlag);
      sb.append("\t");
      sb.append(privileges);
      sb.append("\t");
      sb.append(status);
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param privileges
   *          Privileges
   * @param userStatus
   *          user status
   * @return UserInfo
   * @throws Exception
   *           if an error occurres
   */
  public UserInfo regieterNewUser(String username, String pwHash, String fullname, String adminFlag, String privileges, String userStatus) throws Exception {
    if (users.containsKey(username)) {
      throw new Exception("USER_ALREADY_EXISTS");
    }

    int ret = authenticator.registerByHash(username, pwHash);
    if (ret < 0) {
      throw new Exception("PW_REGISTER_ERROR");
    }

    boolean isAdmin = "1".equals(adminFlag);
    int status = StrUtil.parseInt(userStatus, UserInfo.STATE_NONE);

    UserInfo user = new UserInfo(username, fullname, isAdmin, privileges, status);
    users.put(username, user);

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("USER_REGISTER_IO_ERROR");
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param privileges
   *          Privileges
   * @param userStatus
   *          user status
   * @return UserInfo
   * @throws Exception
   *           if an error occurres
   */
  public UserInfo updateUser(String username, String pwHash, String fullname, String adminFlag, String privileges, String userStatus) throws Exception {
    UserInfo user = users.get(username);
    if (user == null) {
      throw new Exception("NO_SUCH_USER");
    }

    if (adminFlag != null) {
      boolean isAdmin = "1".equals(adminFlag);
      user.setAdmin(isAdmin);
    }

    if (fullname != null) {
      user.setFullName(fullname);
    }

    if (privileges != null) {
      user.setPrivileges(privileges);
    }

    if (userStatus != null) {
      int status = StrUtil.parseInt(userStatus, UserInfo.STATE_NONE);
      user.setStatus(status);
    }

    if (pwHash != null) {
      int ret = authenticator.registerByHash(username, pwHash);
      if (ret < 0) {
        throw new Exception("PW_REGISTER_ERROR");
      }
    }

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("USER_UPDATE_IO_ERROR");
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
    UserInfo user = users.get(username);
    if (user == null) {
      throw new Exception("NO_SUCH_USER");
    }

    authenticator.remove(username);
    users.remove(username);

    try {
      saveUsers();
    } catch (IOException ioe) {
      throw new Exception("USER_DELETE_IO_ERROR");
    }
  }

  private String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

}
