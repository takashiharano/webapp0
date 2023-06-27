/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;

public abstract class Action {

  protected boolean authRequired = true;

  public static Action getActionInstance(ProcessContext context, String actionName) {
    String basePkgName = AppManager.getBasePackageName();
    String pkgName = basePkgName + ".action";
    String[] packages = { "", "system", "system.users", "sample", "sample.async" };

    actionName = actionName.substring(0, 1).toUpperCase() + actionName.substring(1);
    for (int i = 0; i < packages.length; i++) {
      String subPackage = packages[i];
      if (!subPackage.equals("")) {
        subPackage += ".";
      }
      String classFullName = pkgName + "." + subPackage + actionName + "Action";
      Action action = getBean(classFullName);
      if (action != null) {
        action.init(context);
        return action;
      }
    }
    return null;
  }

  private static Action getBean(String className) {
    try {
      Class<?> c = Class.forName(className);
      Action bean = (Action) c.getDeclaredConstructor().newInstance();
      return bean;
    } catch (Exception e) {
      return null;
    }
  }

  public boolean isAuthRequired() {
    return authRequired;
  }

  public void setAuthRequired(boolean authRequired) {
    this.authRequired = authRequired;
  }

  protected void init(ProcessContext context) {
  }

  public abstract void process(ProcessContext context) throws Exception;

}
