/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.user;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.libutil.FileUtil;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.AppManager;

public class GroupManager {

  private static final String GROUPS_FILE_NAME = "groups.txt";

  private static GroupManager instance;
  private Map<String, Group> groups;

  /**
   * Initializes the manager object and loads users info from a storage.
   */
  public GroupManager() {
    init();
  }

  public static GroupManager getInstance() {
    if (instance == null) {
      instance = new GroupManager();
    }
    return instance;
  }

  public void init() {
    loadGroups();
  }

  /**
   * Returns the group info of specified group name.
   * 
   * @param gid
   *          target group id
   * @return the group info
   */
  public Group getGroupInfo(String gid) {
    return groups.get(gid);
  }

  /**
   * Returns all group info map.
   *
   * @return the map of group info
   */
  public Map<String, Group> getAllGroupInfo() {
    return groups;
  }

  /**
   * Write user info into a storage.
   *
   * @throws IOException
   *           if an IO error occurred
   */
  public void saveGroups() throws IOException {
    String header = "#GID\tPrivileges\tDescription\tCreated\tUpdated\n";
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    for (Entry<String, Group> entry : groups.entrySet()) {
      Group group = entry.getValue();
      String gid = group.getGid();
      String privileges = group.getPrivilegesInOneLine();
      String description = group.getDescription();
      long createdDate = group.getCreatedDate();
      long updatedDate = group.getUpdatedDate();

      sb.append(gid);
      sb.append("\t");
      sb.append(privileges);
      sb.append("\t");
      sb.append(description);
      sb.append("\t");
      sb.append(createdDate);
      sb.append("\t");
      sb.append(updatedDate);
      sb.append("\n");
    }

    String dataPath = getDataPath();
    String groupsFilePath = FileUtil.joinPath(dataPath, GROUPS_FILE_NAME);
    String data = sb.toString();
    try {
      FileUtil.write(groupsFilePath, data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  /**
   * Register new group.
   *
   * @param gid
   *          Group id
   * @param privileges
   *          Privileges
   * @param description
   *          user description
   * @return Group
   * @throws Exception
   *           if an error occurred
   */
  public Group regieterNewGroup(String gid, String privileges, String description) throws Exception {
    if (groups.containsKey(gid)) {
      throw new Exception("GROUP_ALREADY_EXISTS");
    }

    long now = System.currentTimeMillis();
    long createdDate = now;
    long updatedDate = now;

    Group group = new Group(gid, privileges, description, createdDate, updatedDate);
    groups.put(gid, group);

    try {
      saveGroups();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_USER_REGISTER");
    }

    return group;
  }

  /**
   * Update a group.
   *
   * @param gid
   *          Group id
   * @param privileges
   *          Privileges
   * @param description
   *          user description
   * @return Group
   * @throws Exception
   *           if an error occurred
   */
  public Group updateGroup(String gid, String privileges, String description) throws Exception {
    Group group = groups.get(gid);
    if (group == null) {
      throw new Exception("GROUP_NOT_FOUND");
    }

    boolean updated = false;

    if (privileges != null) {
      group.setPrivileges(privileges);
      updated = true;
    }

    if (description != null) {
      group.setDescription(description);
      updated = true;
    }

    long now = System.currentTimeMillis();
    if (updated) {
      group.setUpdatedDate(now);
    }

    try {
      saveGroups();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_GROUP_UPDATE");
    }

    return group;
  }

  /**
   * Deletes a group
   *
   * @param gid
   *          Group id
   * @throws Exception
   *           if an error occurred
   */
  public void deleteGroup(String gid) throws Exception {
    Group group = groups.remove(gid);
    if (group == null) {
      throw new Exception("GROUP_NOT_FOUND");
    }

    try {
      saveGroups();
    } catch (IOException ioe) {
      throw new Exception("IO_ERROR_ON_GROUP_DELETE");
    }
  }

  /**
   * Returns if the specified group exists.
   *
   * @param gid
   *          the group id for search
   * @return true if the group exists; otherwise false
   */
  public boolean existsGroup(String gid) {
    return groups.containsKey(gid);
  }

  /**
   * Returns whether the user has a privilege.
   *
   * @param gid
   *          target group id
   * @param privilege
   *          target privilege name
   * @return true if the group has the privilege
   */
  public boolean hasPrivilege(String gid, String privilege) {
    Group group = getGroupInfo(gid);
    if (group != null) {
      if (group.hasPrivilege(privilege)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Load user info from a storage.
   */
  public void loadGroups() {
    groups = new LinkedHashMap<>();
    String dataPath = getDataPath();
    String groupsFilePath = FileUtil.joinPath(dataPath, GROUPS_FILE_NAME);

    String[] text = FileUtil.readTextAsArray(groupsFilePath);
    if (text == null) {
      return;
    }

    for (int i = 0; i < text.length; i++) {
      String line = text[i];
      if (line.startsWith("#")) {
        continue;
      }

      String[] fields = line.split("\t");

      String gid = fields[0];

      String privileges = null;
      if (fields.length > 1) {
        privileges = fields[1];
      }

      String description = null;
      if (fields.length > 2) {
        description = fields[2];
      }

      Group group = new Group(gid, privileges, description);

      if (fields.length > 3) {
        long createdDate = StrUtil.parseLong(fields[3]);
        group.setCreatedDate(createdDate);
      }

      if (fields.length > 4) {
        long updatedDate = StrUtil.parseLong(fields[4]);
        group.setUpdatedDate(updatedDate);
      }

      groups.put(gid, group);
    }
  }

  private String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

}
