/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.util.LinkedHashSet;
import java.util.Set;

public class Group {

  private String groupName;
  private Set<String> privileges;

  public Group(String groupName, String privileges) {
    this.groupName = groupName;
    setPrivileges(privileges);
  }

  /**
   * Returns the group name.
   *
   * @return group name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the group name.
   *
   * @param groupName
   *          group name
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
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

}
