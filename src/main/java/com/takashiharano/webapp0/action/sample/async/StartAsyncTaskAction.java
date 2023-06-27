/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action.sample.async;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.async.task.SampleAsynkTask;
import com.takashiharano.webapp0.util.Log;

public class StartAsyncTaskAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    int n = context.getRequestParameterAsInteger("n");

    AsyncTaskManager asyncTaskManager = context.getAsyncManager();

    SampleAsynkTask task = new SampleAsynkTask(context, n);
    String taskId = asyncTaskManager.registerTask(task);
    Log.i("AsyncTask start: taskId=" + taskId);
    task.exec();

    JsonBuilder jb = new JsonBuilder();
    jb.append("taskId", taskId);

    String json = jb.toString();

    context.sendJsonResponse("OK", json);
  }

}
