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
    if (appManager.isConfigTrue("pseudo_auth")) {
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
      String[] fields = line.split("\t");

      String username = fields[0];

      boolean isAdmin = false;
      String name = null;
      String permissions = null;

      String adminFlag = "";
      if (fields.length > 1) {
        adminFlag = fields[1];
        isAdmin = "1".equals(adminFlag);
      }

      if (fields.length > 2) {
        name = fields[2];
      }

      if (fields.length > 3) {
        permissions = fields[3];
      }

      UserInfo userInfo = new UserInfo(username, isAdmin, name, permissions);
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
    StringBuilder sb = new StringBuilder();
    for (Entry<String, UserInfo> entry : users.entrySet()) {
      UserInfo user = entry.getValue();
      String username = user.getUsername();
      boolean isAdmin = user.isAdministrator();
      String name = user.getName();
      String permissions = user.getPermissionsInOneLine();
      String adminFlag = (isAdmin ? "1" : "0");
      sb.append(username);
      sb.append("\t");
      sb.append(adminFlag);
      sb.append("\t");
      sb.append(name);
      sb.append("\t");
      sb.append(permissions);
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param name
   *          Name
   * @param permissions
   *          Permissions
   * @return UserInfo
   * @throws Exception
   *           if an error occurres
   */
  public UserInfo regieterNewUser(String username, String pwHash, String adminFlag, String name, String permissions) throws Exception {
    if (users.containsKey(username)) {
      throw new Exception("USER_ALREADY_EXISTS");
    }

    int ret = authenticator.registerByHash(username, pwHash);
    if (ret != -1) {
      throw new Exception("PW_REGISTER_ERROR");
    }

    boolean isAdmin = "1".equals(adminFlag);

    UserInfo user = new UserInfo(username, isAdmin, name, permissions);
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
   * @param adminFlag
   *          Administrator flag. 1=admin / 0=otherwise
   * @param name
   *          Name
   * @param permissions
   *          Permissions
   * @return UserInfo
   * @throws Exception
   *           if an error occurres
   */
  public UserInfo updateUser(String username, String pwHash, String adminFlag, String name, String permissions) throws Exception {
    UserInfo user = users.get(username);
    if (user == null) {
      throw new Exception("NO_SUCH_USER");
    }

    if (adminFlag != null) {
      boolean isAdmin = "1".equals(adminFlag);
      user.setAdministrator(isAdmin);
    }

    if (name != null) {
      user.setName(name);
    }

    if (permissions != null) {
      user.setPermissions(permissions);
    }

    if (pwHash != null) {
      int ret = authenticator.registerByHash(username, pwHash);
      if (ret != -1) {
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
