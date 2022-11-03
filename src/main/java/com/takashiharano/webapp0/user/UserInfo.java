package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

public class UserInfo {
  private String username;
  private boolean administrator;
  private String name;
  private Set<String> permissions;

  public UserInfo(String username, boolean isAdmin) {
    this.username = username;
    this.administrator = isAdmin;
    this.permissions = new LinkedHashSet<>();
  }

  public UserInfo(String username, boolean isAdmin, String name, String permissions) {
    this.username = username;
    this.administrator = isAdmin;
    this.name = name;
    setPermissions(permissions);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean administrator) {
    this.administrator = administrator;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String[] getPermissions() {
    return permissions.toArray(new String[0]);
  }

  public void setPermissions(LinkedHashSet<String> permissions) {
    this.permissions = permissions;
  }

  public void setPermissions(String permissions) {
    this.permissions = new LinkedHashSet<>();
    if (permissions == null) {
      return;
    }
    String[] p = permissions.trim().split(" ");
    for (int i = 0; i < p.length; i++) {
      String permission = p[i];
      this.permissions.add(permission);
    }
  }

  public void addPermission(String permission) {
    permissions.add(permission);
  }

  public void removePermission(String permission) {
    permissions.remove(permission);
  }

  public boolean hasPermission(String permission) {
    return permissions.contains(permission);
  }

  public String getPermissionsInOneLine() {
    return getPermissionsInOneLine(" ");
  }

  public String getPermissionsInOneLine(String separator) {
    StringBuilder sb = new StringBuilder();
    int cnt = 0;
    for (String p : permissions) {
      if (cnt > 0) {
        sb.append(separator);
      }
      sb.append(p);
      cnt++;
    }
    return sb.toString();
  }

}
