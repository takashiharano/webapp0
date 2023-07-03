/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User Entity.
 */
public class User {
  public static final int STATE_NONE = 0;
  public static final int STATE_DISABLED = 1;
  public static final int STATE_LOCKED = 1 << 1;
  public static final int STATE_NEED_PW_CHANGE = 1 << 2;

  private String username;
  private String fullname;
  private String localFullName;
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

  public User(String username, String fullname, String localFullName, boolean isAdmin, String privileges, int status) {
    this(username, fullname, localFullName, isAdmin, privileges, status, 0L, 0L);
  }

  public User(String username, String fullname, String localFullName, boolean isAdmin, String privileges, int status, long createdDate, long updatedDate) {
    this.username = username;
    this.fullname = fullname;
    this.localFullName = localFullName;
    this.admin = isAdmin;
    setPrivileges(privileges);
    this.status = status;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
  }

  /**
   * Returns username.
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns full name.
   *
   * @return full name
   */
  public String getFullName() {
    return fullname;
  }

  /**
   * Sets full name.
   *
   * @param fullname
   *          full name
   */
  public void setFullName(String fullname) {
    this.fullname = fullname;
  }

  /**
   * Returns local full name.
   *
   * @return Local full name
   */
  public String getLocalFullName() {
    return localFullName;
  }

  /**
   * Sets local full name.
   *
   * @param local
   *          Local full name
   */
  public void setLocalFullName(String localFullName) {
    this.localFullName = localFullName;
  }

  /**
   * Returns whether the user is an administrator.
   *
   * @return true if the user is an administrator
   */
  public boolean isAdmin() {
    return admin;
  }

  /**
   * Sets whether the user is an administrator.
   *
   * @param isAdmin
   *          true if the user is an administrator
   */
  public void setAdmin(boolean isAdmin) {
    this.admin = isAdmin;
  }

  /**
   * Return the user privileges as array.
   *
   * @return the user privileges.
   */
  public String[] getPrivileges() {
    return privileges.toArray(new String[0]);
  }

  /**
   * Sets the user privileges.
   *
   * @param privileges
   *          the user privileges
   */
  public void setPrivileges(LinkedHashSet<String> privileges) {
    this.privileges = privileges;
  }

  /**
   * Sets the user privileges by string.<br>
   *
   * @param privileges
   *          the user privileges. "priv1 priv2 priv3..."
   */
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

  /**
   * Adds a user privilege.
   *
   * @param privilege
   *          a privilege name
   */
  public void addPrivileges(String privilege) {
    privileges.add(privilege);
  }

  /**
   * Removes a user privilege.
   *
   * @param privilege
   *          a privilege name
   */
  public void removePrivilege(String privilege) {
    privileges.remove(privilege);
  }

  /**
   * Returns whether the user has a privilege.
   *
   * @param privilege
   *          target privilege name
   * @return true if the user has the privilege
   */
  public boolean hasPrivilege(String privilege) {
    if (isAdmin()) {
      return true;
    }
    return privileges.contains(privilege);
  }

  /**
   * Returns the user privileges in one line in string.
   *
   * @return the privileges in the format "priv1 priv2 priv3..."
   */
  public String getPrivilegesInOneLine() {
    return getPrivilegesInOneLine(" ");
  }

  /**
   * Returns the user privileges in one line with the given separator in string.
   *
   * @param separator
   *          the separator between privilege names
   * @return the privileges in the format "priv1[SEP]priv2[SEP]priv3..."
   */
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

  /**
   * Returns the user status.
   *
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Sets the user status.
   *
   * @param status
   *          the status
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Sets the state to the user status.
   *
   * @param state
   *          the state to set
   */
  public void setState(int state) {
    this.status |= state;
  }

  /**
   * Unsets the state from the user status.
   *
   * @param state
   *          the state to unset
   */
  public void unsetState(int state) {
    this.status &= ~state;
  }

  /**
   * Returns whether the user has the state.
   *
   * @param state
   *          the state to check
   * @return true if the user has the state
   */
  public boolean hasState(int state) {
    return ((this.status & state) != 0);
  }

  /**
   * Returns the created date of the user.
   *
   * @return the created date in unix millis.
   */
  public long getCreatedDate() {
    return createdDate;
  }

  /**
   * Sets the created date of the user.
   *
   * @param createdDate
   *          the created date in unix millis.
   */
  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * Returns the updated date of the user.
   *
   * @return the updated date in unix millis.
   */
  public long getUpdatedDate() {
    return updatedDate;
  }

  /**
   * Sets the updated date of the user.
   *
   * @param updatedDate
   *          the updated date in unix millis.
   */
  public void setUpdatedDate(long updatedDate) {
    this.updatedDate = updatedDate;
  }

}
