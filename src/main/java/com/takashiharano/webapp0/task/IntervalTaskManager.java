/*
 * THIS CODE IS IMPLEMENTED BASED ON THE webapp0 TEMPLATE.
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
  public int getInterval(String taskName) {
    TaskContext context = taskMap.get(taskName);
    return context.getInterval();
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
    private int interval; // in seconds
    private long lastExecutedTime;
    private Throwable throwable;

    public TaskContext(IntervalTask task, int interval) {
      this.task = task;
      this.thread = new Thread(this);
      this.interval = interval;
    }

    public void start() {
      thread.start();
    }

    public void stop() {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        Log.e(e);
      }
    }

    public int getInterval() {
      return interval;
    }

    public long getLastExecutedTime() {
      return lastExecutedTime;
    }

    public long getNextExecutionTime() {
      return lastExecutedTime + interval * 1000;
    }

    public boolean hasError() {
      return (throwable != null);
    }

    public Throwable getException() {
      return throwable;
    }

    @Override
    public void run() {
      execTask();
    }

    private void execTask() {
      long intervalMillis = interval * 1000;
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
