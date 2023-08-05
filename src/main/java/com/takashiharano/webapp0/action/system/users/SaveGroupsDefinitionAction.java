/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.system.users;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.user.UserManager;

public class SaveGroupsDefinitionAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    if (!context.hasPermission("sysadmin")) {
      context.sendJsonResponse("FORBIDDEN:GetGroupsDefinition", null);
      return;
    }

    String text = context.getBase64DecodedRequestParameter("text");
    UserManager userManager = context.getUserManager();
    userManager.saveGroupsDefinition(text);

    context.sendJsonResponse("OK", null);
  }

}
