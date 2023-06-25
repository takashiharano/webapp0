/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
package com.takashiharano.webapp0.action.system.users;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.user.UserInfo;

public class UserInfoCommonLogic {

  public static String buildUserInfoJson(UserInfo info) {
    JsonBuilder jb = new JsonBuilder();
    jb.append("username", info.getUsername());
    jb.append("fullname", info.getFullName());
    jb.append("is_admin", info.isAdmin());
    jb.append("privileges", info.getPrivilegesInOneLine());
    jb.append("status", info.getStatus());
    jb.append("created_date", info.getCreatedDate());
    jb.append("updated_date", info.getUpdatedDate());
    String json = jb.toString();
    return json;
  }

}
