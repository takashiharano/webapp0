package com.takashiharano.webapp0.session;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
  private String username;
  private boolean isAdministrator;
  private List<String> roleNames;

  public UserInfo(String username) {
    this.username = username;
    this.roleNames = new ArrayList<>();
  }

  public UserInfo(String username, boolean isAdministrator, List<String> roleNames) {
    this.username = username;
    this.isAdministrator = isAdministrator;
    this.roleNames = roleNames;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
  }

  public List<String> getRoleNames() {
    return roleNames;
  }

  public String getRoleNamesInOneLine() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < roleNames.size(); i++) {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(roleNames.get(i));
    }
    return sb.toString();
  }

  public void setRoleNames(List<String> roleNames) {
    this.roleNames = roleNames;
  }

  public void setRoleNames(String roleNames) {
    String[] roles = roleNames.trim().split(" ");
    this.roleNames = new ArrayList<>();
    for (int i = 0; i < roles.length; i++) {
      this.roleNames.add(roles[i]);
    }
  }

  public boolean hasRole(String roleName) {
    if (roleNames == null) {
      return false;
    }
    for (int i = 0; i < roleNames.size(); i++) {
      if (roleNames.get(i).equals(roleName)) {
        return true;
      }
    }
    return false;
  }

}
