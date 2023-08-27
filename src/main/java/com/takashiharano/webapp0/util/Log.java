/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.util;

import java.util.concurrent.ConcurrentHashMap;

import com.libutil.DateTime;
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

  public ProcessContext getContext() {
    Thread th = Thread.currentThread();
    long tid = th.getId();
    ProcessContext context = tidContextMap.get(tid);
    return context;
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

  protected String buildMessage(Object o, LogLevel lv, int stackFrameOffset, boolean printLine) {
    Thread th = Thread.currentThread();
    ProcessContext context = getContext();
    StringBuilder sb = new StringBuilder();

    if ((flag & FLAG_TIME) != 0) {
      String datetime = DateTime.getCurrentString(dateTimeFormat);
      sb.append(datetime);
      sb.append(" ");
    }

    StringBuilder inf = new StringBuilder();
    if ((flag & FLAG_LEVEL) != 0) {
      inf.append("[");
      inf.append(lv.getTypeSymbol());
      inf.append("]");
    }

    if (((flag & FLAG_MODULE_NAME) != 0) && (moduleName != null)) {
      inf.append("[");
      inf.append(moduleName);
      inf.append("]");
    }

    if ((flag & FLAG_TID) != 0) {
      long tid = th.getId();
      inf.append("[tid:");
      inf.append(tid);
      inf.append("]");
    }

    String addr = "-";
    String username = "-";

    if (context != null) {
      addr = context.getRemoteAddr();
      username = context.getUsername();
    }
    if (username == null) {
      username = "-";
    }

    inf.append("[");
    inf.append(addr);
    inf.append("]");

    inf.append("[");
    inf.append(username);
    inf.append("]");

    if (((flag & FLAG_LINE) != 0) && printLine) {
      StackTraceElement[] stack = th.getStackTrace();
      int stackFrameIndex = 4 + stackFrameOffset;
      StackTraceElement frame = stack[stackFrameIndex];
      String method = frame.getMethodName() + "()";
      String fileName = frame.getFileName();
      int line = frame.getLineNumber();
      String fileLine = method + ":" + fileName + ":" + line;
      inf.append("[");
      inf.append(fileLine);
      inf.append("]");
    }

    if (inf.length() > 0) {
      sb.append(inf);
      sb.append(" ");
    }
    sb.append(dump(o));

    return sb.toString();
  }

}
