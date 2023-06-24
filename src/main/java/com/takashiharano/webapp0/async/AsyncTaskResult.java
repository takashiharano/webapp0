/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
package com.takashiharano.webapp0.async;

public class AsyncTaskResult {

  private String strResult;
  private int intResult;
  private long longResult;
  private float floatResult;
  private double doubleResult;
  private boolean booleanResult;
  private Object objectResult;

  public AsyncTaskResult() {
  }

  public AsyncTaskResult(String result) {
    strResult = result;
  }

  public AsyncTaskResult(int result) {
    intResult = result;
  }

  public AsyncTaskResult(long result) {
    longResult = result;
  }

  public AsyncTaskResult(float result) {
    floatResult = result;
  }

  public AsyncTaskResult(double result) {
    doubleResult = result;
  }

  public AsyncTaskResult(boolean result) {
    booleanResult = result;
  }

  public AsyncTaskResult(Object result) {
    objectResult = result;
  }

  public String getStringResult() {
    return strResult;
  }

  public int getIntResult() {
    return intResult;
  }

  public long getLongResult() {
    return longResult;
  }

  public float getFloatResult() {
    return floatResult;
  }

  public double getDoubleResult() {
    return doubleResult;
  }

  public boolean getBooleanResult() {
    return booleanResult;
  }

  public Object getObjectResult() {
    return objectResult;
  }

  public void setResult(String result) {
    strResult = result;
  }

  public void setResult(int result) {
    intResult = result;
  }

  public void setResult(long result) {
    longResult = result;
  }

  public void setResult(float result) {
    floatResult = result;
  }

  public void setResult(double result) {
    doubleResult = result;
  }

  public void setResult(boolean result) {
    booleanResult = result;
  }

  public void setResult(Object result) {
    objectResult = result;
  }
}
