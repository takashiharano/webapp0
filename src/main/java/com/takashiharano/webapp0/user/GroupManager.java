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

import com.libutil.CsvBuilder;
import com.libutil.FileUtil;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.util.CsvFieldGetter;

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
   * Register new group.
   *
   * @param gid
   *          Group id
   * @param name
   *          Group name
   * @param privileges
   *          Privileges
   * @param description
   *          user description
   * @return Group
   * @throws Exception
   *           if an error occurred
   */
  public Group regieterNewGroup(String gid, String name, String privileges, String description) throws Exception {
    if (groups.containsKey(gid)) {
      throw new Exception("GROUP_ALREADY_EXISTS");
    }

    long now = System.currentTimeMillis();
    long createdDate = now;
    long updatedDate = now;

    Group group = new Group(gid, name, privileges, description, createdDate, updatedDate);
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
   * @param name
   *          Group name
   * @param privileges
   *          Privileges
   * @param description
   *          user description
   * @return Group
   * @throws Exception
   *           if an error occurred
   */
  public Group updateGroup(String gid, String name, String privileges, String description) throws Exception {
    Group group = groups.get(gid);
    if (group == null) {
      throw new Exception("GROUP_NOT_FOUND");
    }

    boolean updated = false;

    if (name != null) {
      group.setName(name);
      updated = true;
    }

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

    String[] lines = FileUtil.readTextAsArray(groupsFilePath);
    if (lines == null) {
      return;
    }

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.startsWith("#")) {
        continue;
      }

      CsvFieldGetter csvFieldGetter = new CsvFieldGetter(line);
      String gid = csvFieldGetter.getFieldValue();
      String name = csvFieldGetter.getFieldValue();
      String privileges = csvFieldGetter.getFieldValue();
      String description = csvFieldGetter.getFieldValue();
      long createdDate = csvFieldGetter.getFieldValueAsLong();
      long updatedDate = csvFieldGetter.getFieldValueAsLong();

      Group group = new Group(gid, name, privileges, description);
      group.setCreatedDate(createdDate);
      group.setUpdatedDate(updatedDate);

      groups.put(gid, group);
    }
  }

  /**
   * Write group info into a storage.
   *
   * @throws IOException
   *           if an IO error occurred
   */
  public void saveGroups() throws IOException {
    String header = "#GID\tName\tPrivileges\tDescription\tCreated\tUpdated\n";

    CsvBuilder csvBuilder = new CsvBuilder("\t", false);
    csvBuilder.appendAsIs(header);

    for (Entry<String, Group> entry : groups.entrySet()) {
      Group group = entry.getValue();
      String gid = group.getGid();
      String name = group.getName();
      String privileges = group.getPrivilegesInOneLine();
      String description = group.getDescription();
      long createdDate = group.getCreatedDate();
      long updatedDate = group.getUpdatedDate();

      csvBuilder.append(gid);
      csvBuilder.append(name);
      csvBuilder.append(privileges);
      csvBuilder.append(description);
      csvBuilder.append(createdDate);
      csvBuilder.append(updatedDate);
      csvBuilder.nextRecord();
    }

    String dataPath = getDataPath();
    String groupsFilePath = FileUtil.joinPath(dataPath, GROUPS_FILE_NAME);
    String data = csvBuilder.toString();
    try {
      FileUtil.write(groupsFilePath, data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    }
  }

  private String getDataPath() {
    AppManager appManager = AppManager.getInstance();
    String workspacePath = appManager.getAppWorkspacePath();
    return workspacePath;
  }

}
