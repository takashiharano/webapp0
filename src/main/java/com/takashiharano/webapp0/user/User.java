/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

public class User {
  public static final int STATE_NONE = 0;
  public static final int STATE_DISABLED = 1;
  public static final int STATE_LOCKED = 1 << 1;
  public static final int STATE_NEED_PW_CHANGE = 1 << 2;

  private String username;
  private String fullname;
  private boolean admin;
  private Set<String> privileges;
  private int status;
  private long createdDate;
  private long updatedDate;

  public User(String username, boolean isAdmin) {
    this.username = username;
    this.admin = isAdmin;
    this.privileges = new LinkedHashSet<>();
    this.status = STATE_NONE;
  }

  public User(String username, String fullname, boolean isAdmin, String privileges, int status) {
    this(username, fullname, isAdmin, privileges, status, 0L, 0L);
  }

  public User(String username, String fullname, boolean isAdmin, String privileges, int status, long createdDate, long updatedDate) {
    this.username = username;
    this.fullname = fullname;
    this.admin = isAdmin;
    setPrivileges(privileges);
    this.status = status;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullName() {
    return fullname;
  }

  public void setFullName(String fullname) {
    this.fullname = fullname;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean isAdmin) {
    this.admin = isAdmin;
  }

  public String[] getPrivileges() {
    return privileges.toArray(new String[0]);
  }

  public void setPrivileges(LinkedHashSet<String> privileges) {
    this.privileges = privileges;
  }

  public void setPrivileges(String privileges) {
    this.privileges = new LinkedHashSet<>();
    if (privileges == null) {
      return;
    }
    String[] p = privileges.trim().split(" ");
    for (int i = 0; i < p.length; i++) {
      String privilege = p[i];
      this.privileges.add(privilege);
    }
  }

  public void addPrivileges(String privilege) {
    privileges.add(privilege);
  }

  public void removePrivilege(String privilege) {
    privileges.remove(privilege);
  }

  public boolean hasPrivilege(String privilege) {
    if (isAdmin()) {
      return true;
    }
    return privileges.contains(privilege);
  }

  public String getPrivilegesInOneLine() {
    return getPrivilegesInOneLine(" ");
  }

  public String getPrivilegesInOneLine(String separator) {
    StringBuilder sb = new StringBuilder();
    int cnt = 0;
    for (String p : privileges) {
      if (cnt > 0) {
        sb.append(separator);
      }
      sb.append(p);
      cnt++;
    }
    return sb.toString();
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setState(int state) {
    this.status |= state;
  }

  public void unsetState(int state) {
    this.status &= ~state;
  }

  public boolean hasState(int state) {
    return ((this.status & state) != 0);
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public long getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(long updatedDate) {
    this.updatedDate = updatedDate;
  }

}
