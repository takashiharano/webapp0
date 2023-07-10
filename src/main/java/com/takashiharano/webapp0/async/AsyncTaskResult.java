/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.async;

public class AsyncTaskResult {

  private Object result;
  private Throwable exception;

  public AsyncTaskResult() {
    super();
  }

  public AsyncTaskResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return this.result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Throwable getException() {
    return exception;
  }

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public boolean hasError() {
    if (exception == null) {
      return false;
    }
    return true;
  }

}
