/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.async;

public class AsyncTaskResult {

  private Object result;

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
}
