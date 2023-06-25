/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.task;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.takashiharano.webapp0.util.Log;

public class IntervalTaskManager {
  private Map<String, TaskContext> taskMap;

  public IntervalTaskManager() {
    taskMap = new ConcurrentHashMap<>();
  }

  /**
   * Start the interval task.
   *
   * @param taskName
   *          task name
   * @param task
   *          interval task
   * @param interval
   *          interval in seconds
   */
  public void startTask(String taskName, IntervalTask task, int interval) {
    Log.i("Starting task: " + taskName + " (" + interval + "s)");
    TaskContext context = new TaskContext(task, interval);
    TaskContext prevTask = taskMap.put(taskName, context);
    if (prevTask != null) {
      prevTask.stop();
    }
    context.start();
  }

  /**
   * Stop the task.
   *
   * @param taskName
   *          task name
   */
  public void stopTask(String taskName) {
    TaskContext context = taskMap.get(taskName);
    context.stop();
    Log.i("The task has been stopped: " + taskName);
  }

  /**
   * Stop all tasks.
   */
  public void stopAllTasks() {
    if (taskMap.size() > 0) {
      Log.i("Stopping all tasks...");
      for (Entry<String, TaskContext> entry : taskMap.entrySet()) {
        String id = entry.getKey();
        stopTask(id);
      }
    }
  }

  /**
   * Returns interval value.
   *
   * @param taskName
   *          task name
   * @return interval in seconds
   */
  public int getIntervalSec(String taskName) {
    TaskContext context = taskMap.get(taskName);
    return context.getIntervalSec();
  }

  /**
   * Returns the last executed time.
   *
   * @param taskName
   *          task name
   * @return last executed time-stamp in milliseconds
   */
  public long getLastExecutedTime(String taskName) {
    TaskContext context = taskMap.get(taskName);
    long lastTime = context.getLastExecutedTime();
    return lastTime;
  }

  /**
   * Returns the next execution time.
   *
   * @param taskName
   *          task name
   * @return next execution time-stamp in milliseconds.
   */
  public long getNextExecutionTime(String taskName) {
    TaskContext context = taskMap.get(taskName);
    long nextTime = context.getNextExecutionTime();
    return nextTime;
  }

  /**
   * Returns if the task has error.
   *
   * @param taskName
   *          task name
   * @return true if the task has error
   */
  public boolean hasError(String taskName) {
    TaskContext context = taskMap.get(taskName);
    return context.hasError();
  }

  /**
   * Returns the exception.
   *
   * @param taskName
   *          task name
   * @return exception
   */
  public Throwable getException(String taskName) {
    TaskContext context = taskMap.get(taskName);
    return context.getException();
  }

  private static class TaskContext implements Runnable {
    private IntervalTask task;
    private Thread thread;
    private int intervalSec;
    private long lastExecutedTime;
    private Throwable throwable;

    public TaskContext(IntervalTask task, int interval) {
      this.task = task;
      this.thread = new Thread(this);
      this.intervalSec = interval;
    }

    /**
     * Starts the task in a sub thread.
     */
    public void start() {
      thread.start();
    }

    /**
     * Stops the task.
     */
    public void stop() {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        Log.e(e);
      }
    }

    /**
     * Returns the interval value in seconds.
     *
     * @return the interval value
     */
    public int getIntervalSec() {
      return intervalSec;
    }

    /**
     * Returns the last executed time in unix millis.
     *
     * @return the last executed time.
     */
    public long getLastExecutedTime() {
      return lastExecutedTime;
    }

    /**
     * Returns the next execution time in unix millis.
     *
     * @return the next execution time
     */
    public long getNextExecutionTime() {
      return lastExecutedTime + intervalSec * 1000;
    }

    /**
     * Returns whether an error occurred.
     *
     * @return if any error occurred
     */
    public boolean hasError() {
      return (throwable != null);
    }

    /**
     * Returns the exception during the execution.
     *
     * @return the exception. if any error has not occurred, returns null.
     */
    public Throwable getException() {
      return throwable;
    }

    @Override
    public void run() {
      execTask();
    }

    private void execTask() {
      long intervalMillis = intervalSec * 1000;
      while (true) {
        if (Thread.interrupted()) {
          break;
        }
        lastExecutedTime = System.currentTimeMillis();
        try {
          task.exec();
        } catch (Throwable t) {
          throwable = t;
          break;
        }
        try {
          Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

}
