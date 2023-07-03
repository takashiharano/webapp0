/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.sample.async;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.async.AsyncTask;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.async.AsyncTaskResult;

public class GetAsyncTaskResultAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String taskId = context.getRequestParameter("taskId");

    AsyncTaskManager asyncTaskManager = context.getAsyncManager();
    AsyncTask task = asyncTaskManager.getAsyncTask(taskId);
    if (task == null) {
      context.sendJsonResponse("NO_TASK_DATA", null);
      return;
    }

    AsyncTaskResult taskResult = asyncTaskManager.getTaskResult(taskId);
    String result = "";
    if (taskResult != null) {
      result = (String) taskResult.getResult();
    }

    JsonBuilder jb = new JsonBuilder();
    jb.append("taskId", taskId);
    jb.append("result", result);

    String json = jb.toString();

    context.sendJsonResponse("OK", json);
  }

}
