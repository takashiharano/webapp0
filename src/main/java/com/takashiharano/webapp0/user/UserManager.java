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

    AppManager appManager = AppManager.getInstance();

    String result;
    if (appManager.isConfigTrue("pseudo_auth")) {
      result = "OK";
    } else {
      String pwHash = HashUtil.sha256(pass + username);
      result = auth.auth(username, pwHash);
    }

    return result;
  }

  public void loadUsers() {
    users = new ConcurrentHashMap<>();
    AppManager appManager = AppManager.getInstance();
    String homePath = appManager.getAppHomePath();
    String userFile = FileUtil.joinPath(homePath, "userpass.txt");

    auth = new Auth(userFile, 1);

    String[] text = FileUtil.readTextAsArray(userFile);
    for (int i = 0; i < text.length; i++) {
      String line = text[i];
      String[] fields = line.split("\t");
      String username = fields[0];
      UserInfo userInfo = new UserInfo(username);
      users.put(username, userInfo);
    }
  }

}
