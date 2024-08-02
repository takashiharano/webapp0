/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.util.AppUtil;

/**
 * Group entity.
 */
public class Group {
  private String gid;
  private String name;
  private Set<String> privileges;
  private String description;
  private long createdDate;
  private long updatedDate;

  public Group(String gid, String name, String privileges, String description) {
    this(gid, name, privileges, description, 0L, 0L);
  }

  public Group(String gid, String name, String privileges, String description, long createdDate, long updatedDate) {
    this.gid = gid;
    this.name = name;
    setPrivileges(privileges);
    this.description = description;
    this.setCreatedDate(createdDate);
    this.setUpdatedDate(updatedDate);
  }

  /**
   * Returns the group id.
   *
   * @return group id
   */
  public String getGid() {
    return gid;
  }

  /**
   * Sets the group id.
   *
   * @param gid
   *          group id
   */
  public void setGid(String gid) {
    this.gid = gid;
  }

  /**
   * Returns the group name.
   *
   * @return group name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the group name.
   *
   * @param name
   *          group name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Return the user privileges as array.
   *
   * @return the user privileges
   */
  public Set<String> getPrivileges() {
    return privileges;
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
    return AppUtil.convertSetToOneLineString(privileges, separator);
  }

  /**
   * Sets the user privileges.
   *
   * @param privileges
   *          the user privileges
   */
  public void setPrivileges(Set<String> privileges) {
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
   * Returns whether the user has a privilege.
   *
   * @param privilege
   *          target privilege name
   * @return true if the user has the privilege
   */
  public boolean hasPrivilege(String privilege) {
    return privileges.contains(privilege);
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description
   *          the description
   */
  public void setDescription(String description) {
    this.description = description;
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
   * Returns all properties in JSON.
   *
   * @return JSON string
   */
  public String toJSON() {
    JsonBuilder jb = new JsonBuilder();
    jb.append("gid", getGid());
    jb.append("name", getName());
    jb.append("privileges", getPrivilegesInOneLine());
    jb.append("description", getDescription());
    jb.append("created_at", getCreatedDate());
    jb.append("updated_at", getUpdatedDate());
    String json = jb.toString();
    return json;
  }

}
