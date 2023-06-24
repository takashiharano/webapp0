/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
 */
package com.takashiharano.webapp0.async.task;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.async.AsyncTask;
import com.takashiharano.webapp0.async.AsyncTaskResult;
import com.takashiharano.webapp0.util.Log;

public class SampleAsynkTask extends AsyncTask {

  private int arg1;

  public SampleAsynkTask(ProcessContext context, int arg1) {
    super(context);
    this.arg1 = arg1;
  }

  @Override
  protected AsyncTaskResult process() throws Exception {
    this.setTaskInfo("loop = 0");

    Log.i("number of loop = " + arg1);
    for (int i = 1; i <= arg1; i++) {
      this.setTaskInfo("loop = " + i);

      if ((i % 10) == 0) {
        Log.i("loop: " + i);
      }

      try {
        Thread.sleep(100);
      } catch (Exception e) {
        Log.e(e);
      }
    }
    Log.i("done");

    AsyncTaskResult result = new AsyncTaskResult();
    result.setResult("RESULT_OK");

    return result;
  }

}
