package com.takashiharano.webapp0.action.sample;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.async.AsyncTask;
import com.takashiharano.webapp0.async.AsyncTaskManager;

public class GetAsyncTaskInfoAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    String taskId = context.getRequestParameter("taskId");

    AsyncTaskManager asyncTaskManager = context.getAsyncManager();
    AsyncTask task = asyncTaskManager.getAsyncTask(taskId);
    if (task == null) {
      context.sendJsonResponse("NO_TASK_DATA", null);
      return;
    }

    String info = (String) task.getTaskInfo();
    boolean isDone = task.isDone();

    JsonBuilder jb = new JsonBuilder();
    jb.append("taskId", taskId);
    jb.append("isDone", isDone);
    jb.append("info", info);

    String json = jb.toString();

    context.sendJsonResponse("OK", json);
  }

}
