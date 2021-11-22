package com.takashiharano.webapp0.util;

import java.util.concurrent.ConcurrentHashMap;

import com.libutil._Log;
import com.takashiharano.webapp0.ProcessContext;

public class Log extends _Log {

  private ConcurrentHashMap<Long, ProcessContext> tidContextMap;

  public Log() {
    tidContextMap = new ConcurrentHashMap<>();
  }

  public static void setup(int level, String moduleName) {
    instance = new Log();
    Log.setLevel(level);
    Log.setModuleName(moduleName);
  }

  public static synchronized Log getInstance() {
    if (instance == null) {
      instance = new Log();
    }
    return (Log) instance;
  }

  public static void setContext(ProcessContext context) {
    getInstance()._setContext(context);
  }

  public void _setContext(ProcessContext context) {
    Thread th = Thread.currentThread();
    long tid = th.getId();
    tidContextMap.put(tid, context);
  }

  public static void removeContext() {
    getInstance()._removeContext();
  }

  /**
   * Unbind the TID from the processing context.
   */
  public void _removeContext() {
    Thread th = Thread.currentThread();
    long tid = th.getId();
    tidContextMap.remove(tid);
  }

}
