package com.takashiharano.webapp1.action.system;

import java.io.IOException;

import com.takashiharano.util.JsonBuilder;
import com.takashiharano.webapp1.AppManager;
import com.takashiharano.webapp1.ProcessContext;
import com.takashiharano.webapp1.action.Action;

public class VersionAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String moduleName = AppManager.getModuleName();
    String version = null;
    try {
      version = AppManager.getManifestEntry(context, "App-Version");
    } catch (IOException e) {
      e.printStackTrace();
    }
    JsonBuilder jb = new JsonBuilder();
    jb.append("module", moduleName);
    jb.append("version", version);
    context.sendJson(jb);
  }

}
