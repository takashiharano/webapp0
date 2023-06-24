/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.action.system;

import java.io.IOException;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;

public class VersionAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
  }

  @Override
  public void process(ProcessContext context) throws Exception {
    String moduleName = AppManager.getModuleName();
    String version = null;
    try {
      version = context.getManifestEntry("App-Version");
    } catch (IOException e) {
      e.printStackTrace();
    }
    JsonBuilder jb = new JsonBuilder();
    jb.append("module", moduleName);
    jb.append("version", version);
    context.sendJson(jb);
  }

}
