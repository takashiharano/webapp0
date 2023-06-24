/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
package com.takashiharano.webapp0.action.sample;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.async.AsyncTaskManager;

public class CancelAsyncTaskAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String taskId = context.getRequestParameter("taskId");

    AsyncTaskManager asyncTaskManager = context.getAsyncManager();
    boolean canceled = asyncTaskManager.cancel(taskId);

    JsonBuilder jb = new JsonBuilder();
    jb.append("taskId", taskId);
    jb.append("canceled", canceled);

    String json = jb.toString();

    context.sendJsonResponse("OK", json);
  }

}
