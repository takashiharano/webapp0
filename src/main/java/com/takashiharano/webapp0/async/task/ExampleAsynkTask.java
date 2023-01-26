package com.takashiharano.webapp0.async.task;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.async.AsyncTask;
import com.takashiharano.webapp0.async.AsyncTaskResult;
import com.takashiharano.webapp0.util.Log;

public class ExampleAsynkTask extends AsyncTask {

  private ProcessContext context;
  private int arg1;

  public ExampleAsynkTask(ProcessContext context, int arg1) {
    this.context = context;
  }

  @Override
  protected AsyncTaskResult process() throws Exception {
    Log.setContext(context);

    this.setTaskInfo("loop = 0");

    Log.i("number of loop = " + arg1);
    for (int i = 0; i < arg1; i++) {
      this.setTaskInfo("loop = " + 0);

      if ((i % 100) == 0) {
        Log.i("loop: " + i);
      }

      try {
        Thread.sleep(100);
      } catch (Exception e) {
        Log.e(e);
      }
    }

    AsyncTaskResult result = new AsyncTaskResult();
    result.setResult("RESULT_OK");

    Log.removeContext();
    return result;
  }

}
