/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.util.Util;

/**
 * User entity.
 */
public class User {
  public static final int FLAG_NONE = 0;
  public static final int FLAG_NEED_PW_CHANGE = 1;
  public static final int FLAG_DISABLED = 1 << 1;

  private String username;
  private String fullname;
  private String localFullName;
  private boolean admin;
  private Set<String> groups;
  private Set<String> privileges;
  private String description;
  private int flags;
  private long createdDate;
  private long updatedDate;
  private UserStatus userStatus;

  public User(String username, boolean isAdmin) {
    this.username = username;
    this.admin = isAdmin;
    this.groups = new LinkedHashSet<>();
    this.privileges = new LinkedHashSet<>();
    this.flags = FLAG_NONE;
  }

  public User(String username, String fullname, String localFullName, boolean isAdmin, String groups, String privileges, String description, int flags) {
    this(username, fullname, localFullName, isAdmin, groups, privileges, description, flags, 0L, 0L);
  }

  public User(String username, String fullname, String localFullName, boolean isAdmin, String groups, String privileges, String description, int flags, long createdDate, long updatedDate) {
    this.username = username;
    this.fullname = fullname;
    this.localFullName = localFullName;
    this.admin = isAdmin;
    setGroups(groups);
    setPrivileges(privileges);
    setDescription(description);
    this.flags = flags;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.userStatus = new UserStatus(username);
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
    return Util.convertSetToOneLineString(groups, separator);
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
    return Util.convertSetToOneLineString(privileges, separator);
  }

  /**
   * Returns whether the current user has the specified privilege.<br>
   * True if the user or a group to which the user belongs has the privilege.
   *
   * @param privilege
   *          the privilege to check
   * @return true if the user has the privilege. always true if the user is admin.
   */
  public boolean hasPermission(String privilege) {
    if (hasPrivilege(privilege)) {
      return true;
    }

    GroupManager groupManager = GroupManager.getInstance();
    for (String gid : groups) {
      if (groupManager.hasPrivilege(gid, privilege)) {
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
    if (description == null) {
      description = "";
    }
    description = description.replaceAll("\\t|\\r\\n|\\n", " ");
    this.description = description;
  }

  /**
   * Returns the user flags.
   *
   * @return the flags
   */
  public int getFlags() {
    return flags;
  }

  /**
   * Sets the user flags.
   *
   * @param flags
   *          the flags
   */
  public void setFlags(int flags) {
    this.flags = flags;
  }

  /**
   * Sets the flag to the user flags.
   *
   * @param flag
   *          the flag to set
   */
  public void setFlag(int flag) {
    this.flags |= flag;
  }

  /**
   * Unsets the flag from the user flags.
   *
   * @param flag
   *          the flag to unset
   */
  public void unsetFlag(int flag) {
    this.flags &= ~flag;
  }

  /**
   * Returns whether the user has the flag.
   *
   * @param flag
   *          the flag to check
   * @return true if the user has the flag
   */
  public boolean hasFlag(int flag) {
    return ((this.flags & flag) != 0);
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

  public UserStatus getUserStatus() {
    return userStatus;
  }

  public void setUserStatus(UserStatus userStatus) {
    this.userStatus = userStatus;
  }

  public String toJSON() {
    return toJSON(false);
  }

  /**
   * Returns all properties in JSON.
   *
   * @param includeStatusInfo
   *          If true, include status info
   * @return JSON string
   */
  public String toJSON(boolean includeStatusInfo) {
    JsonBuilder jb = new JsonBuilder();
    jb.append("username", getUsername());
    jb.append("fullname", getFullName());
    jb.append("localfullname", getLocalFullName());
    jb.append("is_admin", isAdmin());
    jb.append("groups", getGroupsInOneLine());
    jb.append("privileges", getPrivilegesInOneLine());
    jb.append("description", getDescription());
    jb.append("flags", getFlags());
    jb.append("created_date", getCreatedDate());
    jb.append("updated_date", getUpdatedDate());

    if (includeStatusInfo) {
      String jb1 = userStatus.toJSON();
      jb.appendObject("status_info", jb1);
    }

    String json = jb.toString();
    return json;
  }

}
