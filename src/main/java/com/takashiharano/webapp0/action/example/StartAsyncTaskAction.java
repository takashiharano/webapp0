package com.takashiharano.webapp0.action.example;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.async.task.ExampleAsynkTask;

public class StartAsyncTaskAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    int n = context.getRequestParameterAsInt("n");

    AsyncTaskManager asyncTaskManager = context.getAsyncManager();

    ExampleAsynkTask task = new ExampleAsynkTask(context, n);
    String taskId = asyncTaskManager.registerTask(task);
    task.exec();

    JsonBuilder jb = new JsonBuilder();
    jb.append("taskId", taskId);

    String json = jb.toString();

    context.sendJsonResponse("OK", json);
  }

}
