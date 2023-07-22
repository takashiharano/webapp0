/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

import com.libutil.JsonBuilder;

/**
 * User Entity.
 */
public class User {
  public static final int STATE_NONE = 0;
  public static final int STATE_NEED_PW_CHANGE = 1;
  public static final int STATE_DISABLED = 1 << 1;
  public static final int STATE_LOCKED = 1 << 2;

  private String username;
  private String fullname;
  private String localFullName;
  private boolean admin;
  private Set<String> groups;
  private Set<String> privileges;
  private String description;
  private int status;
  private long createdDate;
  private long updatedDate;
  private long pwChangedDate;

  public User(String username, boolean isAdmin) {
    this.username = username;
    this.admin = isAdmin;
    this.groups = new LinkedHashSet<>();
    this.privileges = new LinkedHashSet<>();
    this.status = STATE_NONE;
  }

  public User(String username, String fullname, String localFullName, boolean isAdmin, String groups, String privileges, String description, int status) {
    this(username, fullname, localFullName, isAdmin, groups, privileges, description, status, 0L, 0L, 0L);
  }

  public User(String username, String fullname, String localFullName, boolean isAdmin, String groups, String privileges, String description, int status, long createdDate, long updatedDate, long pwChangedDate) {
    this.username = username;
    this.fullname = fullname;
    this.localFullName = localFullName;
    this.admin = isAdmin;
    setGroups(groups);
    setPrivileges(privileges);
    this.description = description;
    this.status = status;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.pwChangedDate = pwChangedDate;
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
   * @param localFullName
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
   * Return the user groups as array.
   *
   * @return the user groups.
   */
  public String[] getGroups() {
    return groups.toArray(new String[0]);
  }

  /**
   * Sets the user groups by string.<br>
   *
   * @param groups
   *          the user groups. "group1 group2 group3..."
   */
  public void setGroups(String groups) {
    if (groups == null) {
      return;
    }
    this.groups = new LinkedHashSet<>();
    String[] p = groups.trim().split(" ");
    for (int i = 0; i < p.length; i++) {
      String groupName = p[i];
      this.groups.add(groupName);
    }
  }

  /**
   * Adds a user group.
   *
   * @param group
   *          a group name
   */
  public void addGroup(String group) {
    groups.add(group);
  }

  /**
   * Removes a user group.
   *
   * @param group
   *          a group name
   */
  public void removeGroup(String group) {
    groups.remove(group);
  }

  /**
   * Returns whether the user belongs to the group.
   *
   * @param group
   *          target group name
   * @return true if the user belongs to the group
   */
  public boolean isBelongToGroup(String group) {
    if (isAdmin()) {
      return true;
    }
    return groups.contains(group);
  }

  /**
   * Returns the user groups in one line in string.
   *
   * @return the groups in the format "group1 group2 group3..."
   */
  public String getGroupsInOneLine() {
    return getGroupsInOneLine(" ");
  }

  /**
   * Returns the user groups in one line with the given separator in string.
   *
   * @param separator
   *          the separator between group names
   * @return the groups in the format "group1[SEP]group2[SEP]group3..."
   */
  public String getGroupsInOneLine(String separator) {
    return convertSetToOneLineString(groups, separator);
  }

  /**
   * Return the user privileges as array.
   *
   * @return the user privileges
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
  public void addPrivilege(String privilege) {
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
    return convertSetToOneLineString(privileges, separator);
  }

  /**
   * Returns whether the current user has the specified privilege.<br>
   * True if the user or a group to which the user belongs has the privilege.
   *
   * @param privilege
   *          the privilege to check
   * @return true if the user has the privilege. always true if the user is admin.
   */
  public boolean isPermitted(String privilege) {
    boolean has = hasPrivilege(privilege);
    if (has) {
      return true;
    }

    UserManager um = UserManager.getInstance();
    for (String groupName : groups) {
      Group group = um.getGroupInfo(groupName);
      if (group == null) {
        continue;
      }
      has = group.hasPrivilege(privilege);
      if (has) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the user description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the user description.
   *
   * @param description
   *          the user description
   */
  public void setDescription(String description) {
    this.description = description;
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

  /**
   * Returns the date the user last changed their password.
   *
   * @return the last changed date in unix millis.
   */
  public long getPwChangedDate() {
    return pwChangedDate;
  }

  /**
   * Sets the date the user last changed their password.
   *
   * @param pwChangedDate
   *          the last changed date in unix millis.
   */
  public void setPwChangedDate(long pwChangedDate) {
    this.pwChangedDate = pwChangedDate;
  }

  /**
   * Returns all properties in JSON.
   *
   * @return JSON string
   */
  public String toJSON() {
    JsonBuilder jb = new JsonBuilder();
    jb.append("username", getUsername());
    jb.append("fullname", getFullName());
    jb.append("localfullname", getLocalFullName());
    jb.append("is_admin", isAdmin());
    jb.append("groups", getGroupsInOneLine());
    jb.append("privileges", getPrivilegesInOneLine());
    jb.append("description", getDescription());
    jb.append("status", getStatus());
    jb.append("created_date", getCreatedDate());
    jb.append("updated_date", getUpdatedDate());
    jb.append("pw_changed_date", getPwChangedDate());
    String json = jb.toString();
    return json;
  }

  private String convertSetToOneLineString(Set<String> items, String separator) {
    StringBuilder sb = new StringBuilder();
    int cnt = 0;
    for (String item : items) {
      if (cnt > 0) {
        sb.append(separator);
      }
      sb.append(item);
      cnt++;
    }
    return sb.toString();
  }

}
