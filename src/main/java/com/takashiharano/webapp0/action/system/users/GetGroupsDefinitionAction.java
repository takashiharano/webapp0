/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.libutil.Base64Util;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;

public class GetGroupsDefinitionAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.hasPermission("sysadmin")) {
      context.sendJsonResponse("FORBIDDEN:GetGroupsDefinition", null);
      return;
    }

    UserManager userManager = context.getUserManager();
    String text = userManager.readGroupsDefinition();
    String b64text = Base64Util.encode(text);

    context.sendJsonResponse("OK", b64text, false);
  }

}
