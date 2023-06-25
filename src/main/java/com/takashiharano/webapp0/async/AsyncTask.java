/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
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

  /**
   * Execute the async task in a subthread.
   *
   * @return a Future object
   */
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

  /**
   * Returns whether the task has been done.
   *
   * @return true if the task has been done.
   */
  public boolean isDone() {
    if (future == null) {
      return false;
    }
    return future.isDone();
  }

  /**
   * Returns whether the task has been cancelled.
   *
   * @return true if the task has been cancelled.
   */
  public boolean isCancelled() {
    if (future == null) {
      return false;
    }
    return future.isCancelled();
  }

  /**
   * Returns the Future object.
   *
   * @return the Future object
   */
  public Future<AsyncTaskResult> getFuture() {
    return future;
  }

  /**
   * Returns the task info.
   *
   * @return the task info
   */
  public Object getTaskInfo() {
    return taskInfo;
  }

  /**
   * Sets a task info.
   *
   * @param taskInfo
   *          the task info
   */
  public void setTaskInfo(Object taskInfo) {
    this.taskInfo = taskInfo;
  }

  /**
   * Returns the task result.
   *
   * @return the task result
   */
  public AsyncTaskResult getResult() {
    return taskResult;
  }

  /**
   * Returns the task started time in unix millis.
   *
   * @return the task started time
   */
  public long getStartedTime() {
    return startedTime;
  }

  /**
   * Returns the task finished time in unix millis.
   *
   * @return the task finished time
   */
  public long getFinishedTime() {
    return finishedTime;
  }

  /**
   * Sets the task finished time in unix millis.
   */
  public void setFinishedTime() {
    finishedTime = System.currentTimeMillis();
  }

  /**
   * Cancel the async task.
   *
   * @return true if the task is cancelled.
   */
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
