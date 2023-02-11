package com.takashiharano.webapp0.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.util.Log;

public abstract class AsyncTask {

  protected Future<AsyncTaskResult> future;
  protected Object taskInfo;
  protected AsyncTaskResult taskResult;
  protected ProcessContext context;
  protected long startedTime = -1;
  protected long finishedTime = -1;

  public AsyncTask(ProcessContext context) {
    this.context = context;
    taskResult = new AsyncTaskResult();
  }

  public Future<AsyncTaskResult> exec() {
    startedTime = System.currentTimeMillis();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    future = executor.submit(() -> {
      Log.setContext(context);
      AsyncTaskResult result = process();
      finishedTime = System.currentTimeMillis();
      Log.removeContext();
      return result;
    });
    return future;
  }

  public boolean isDone() {
    if (future == null) {
      return false;
    }
    return future.isDone();
  }

  public boolean isCancelled() {
    if (future == null) {
      return false;
    }
    return future.isCancelled();
  }

  public Future<AsyncTaskResult> getFuture() {
    return future;
  }

  public Object getTaskInfo() {
    return taskInfo;
  }

  public void setTaskInfo(Object taskInfo) {
    this.taskInfo = taskInfo;
  }

  public AsyncTaskResult getResult() {
    return taskResult;
  }

  public long getStartedTime() {
    return startedTime;
  }

  public long getFinishedTime() {
    return finishedTime;
  }

  public void setFinishedTime() {
    finishedTime = System.currentTimeMillis();
  }

  public boolean cancel() {
    boolean cancelled = future.cancel(true);
    if (cancelled) {
      setFinishedTime();
    }
    return cancelled;
  }

  /**
   * Execute the task.
   *
   * @return task result object
   * @throws Exception
   *           If an error occurs
   */
  protected abstract AsyncTaskResult process() throws Exception;

}
