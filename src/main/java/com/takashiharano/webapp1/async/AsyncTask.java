package com.takashiharano.webapp1.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AsyncTask {

  protected ExecutorService executor;
  protected Future<AsyncTaskResult> future;
  protected AsyncTaskResult taskResult;
  protected long startedTime = -1;
  protected long finishedTime = -1;

  public AsyncTask() {
    executor = Executors.newSingleThreadExecutor();
    taskResult = new AsyncTaskResult();
  }

  public Future<AsyncTaskResult> execTask() {
    startedTime = System.currentTimeMillis();
    future = executor.submit(() -> {
      AsyncTaskResult result = exec();
      finishedTime = System.currentTimeMillis();
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
   * @return
   * @throws Exception
   */
  protected abstract AsyncTaskResult exec() throws Exception;

}
