/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.async;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.takashiharano.webapp0.util.Log;

public class AsyncTaskManager {
  private static volatile AsyncTaskManager instance;

  private ConcurrentHashMap<String, AsyncTask> asyncTaskMap;
  private long counter;

  public AsyncTaskManager() {
    asyncTaskMap = new ConcurrentHashMap<>();
    counter = 0;
  }

  public static AsyncTaskManager getInstance() {
    if (instance == null) {
      instance = new AsyncTaskManager();
    }
    return instance;
  }

  /**
   * Register the async task.
   *
   * @param asyncTask
   *          the instance of AsyncTask implementation
   * @return id of the async task
   */
  public String registerTask(AsyncTask asyncTask) {
    cleanupOldTasks();
    String taskId = "async-task-" + incrementCounter();
    asyncTaskMap.put(taskId, asyncTask);
    return taskId;
  }

  /**
   * Execute the async task.
   *
   * @param taskId
   *          the task id to execute
   * @return true if the task has been executed; false otherwise.
   */
  public boolean executeTask(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    if (asyncTask == null) {
      return false;
    }
    asyncTask.exec();
    return true;
  }

  /**
   * Returns whether the task has been done.
   *
   * @param taskId
   *          target task ID
   * @return true if the task has been done
   */
  public boolean isDone(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    if (asyncTask == null) {
      return false;
    }
    boolean isDone = asyncTask.isDone();
    boolean isCancelled = asyncTask.isCancelled();
    return (isDone || isCancelled);
  }

  /**
   * Returns the task object.
   *
   * @param taskId
   *          task id
   * @return the task object
   */
  public AsyncTask getAsyncTask(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    return asyncTask;
  }

  /**
   * Returns the task info.
   *
   * @param taskId
   *          task id
   * @return task info object
   */
  public Object getAsyncTaskInfo(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    if (asyncTask == null) {
      return null;
    }
    return asyncTask.getTaskInfo();
  }

  /**
   * Returns the task execution result as it was when this method was called.
   *
   * @param taskId
   *          task id
   * @return task result object
   */
  public AsyncTaskResult getResult(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    if (asyncTask == null) {
      return null;
    }
    return asyncTask.getResult();
  }

  /**
   * Returns the task execution result.<br>
   * This method blocks until the task is complete.<br>
   * If you don't want it to block, call this method after isDone () returns true.
   *
   * @param taskId
   *          task id
   * @return task result object
   * @throws Exception
   *           If an error occurs
   */
  public AsyncTaskResult getTaskResult(String taskId) throws Exception {
    AsyncTask asyncTask = asyncTaskMap.remove(taskId);
    if (asyncTask == null) {
      return null;
    }
    Future<AsyncTaskResult> future = asyncTask.getFuture();
    AsyncTaskResult result = null;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new Exception("Get task result error: " + e.toString(), e);
    }
    return result;
  }

  /**
   * Cancel the task.
   *
   * @param taskId
   *          task id
   * @return true if the task is cancelled; false otherwise
   */
  public boolean cancel(String taskId) {
    AsyncTask asyncTask = asyncTaskMap.get(taskId);
    boolean cancelled = false;
    if (asyncTask != null) {
      cancelled = asyncTask.cancel();
      asyncTaskMap.remove(taskId);
    }
    Log.i("AsyncTask cancel: " + taskId + " [" + (cancelled ? "OK" : "NG") + "]");
    return cancelled;
  }

  /**
   * Increment the counter for Task ID.
   *
   * @return Incremented value
   */
  private synchronized long incrementCounter() {
    return ++counter;
  }

  /**
   * Removes tasks that remain in management information for more than a certain
   * amount of time after completion.<br>
   * Running tasks are not deleted.
   */
  private void cleanupOldTasks() {
    long MAX_TIME = 300000; // 5min
    long now = System.currentTimeMillis();

    for (Entry<String, AsyncTask> entry : asyncTaskMap.entrySet()) {
      String taskId = entry.getKey();
      AsyncTask task = entry.getValue();
      long finishedTime = task.getFinishedTime();
      if (finishedTime < 0) {
        continue;
      }
      long elapsed = now - finishedTime;
      if (elapsed > MAX_TIME) {
        asyncTaskMap.remove(taskId);
        Log.i("Old task removed: " + taskId);
      }
    }

    int taskNum = asyncTaskMap.size();
    if (taskNum == 0) {
      counter = 0;
    }
  }
}
