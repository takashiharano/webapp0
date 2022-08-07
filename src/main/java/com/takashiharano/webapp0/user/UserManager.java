package com.takashiharano.webapp0.user;

import java.util.concurrent.ConcurrentHashMap;

import com.libutil.FileUtil;
import com.libutil.HashUtil;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.auth.Auth;

public class UserManager {

  private static UserManager instance;
  private ConcurrentHashMap<String, UserInfo> users;
  private Auth auth;

  public static UserManager getInstance() {
    if (instance == null) {
      instance = new UserManager();
    }
    return instance;
  }

  public UserInfo getUserInfo(String username) {
    return users.get(username);
  }

  public String authenticate(String username, String pass) {
    if (StrUtil.isBlank(username) || StrUtil.isBlank(pass)) {
      return "NG";
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
      result = auth.auth(username, pwHash);
    }

    return result;
  }

  public void loadUsers() {
    users = new ConcurrentHashMap<>();
    AppManager appManager = AppManager.getInstance();
    String homePath = appManager.getAppHomePath();
    String usersFile = FileUtil.joinPath(homePath, "users.txt");
    String passFile = FileUtil.joinPath(homePath, "userpass.txt");
    auth = new Auth(passFile, 1);

    String[] text = FileUtil.readTextAsArray(usersFile);
    for (int i = 0; i < text.length; i++) {
      String line = text[i];
      String[] fields = line.split("\t");

      String username = fields[0];
      String admin = "";
      if (fields.length >= 2) {
        admin = fields[1];
      }
      boolean isAdmin = "1".equals(admin);

      UserInfo userInfo = new UserInfo(username, isAdmin);
      users.put(username, userInfo);
    }
  }

}
