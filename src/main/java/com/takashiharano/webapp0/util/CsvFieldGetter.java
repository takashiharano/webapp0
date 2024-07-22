/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.util;

public class CsvFieldGetter {

  private String[] fields;
  private int index;

  public CsvFieldGetter(String line) {
    this(line, "\t");
  }

  public CsvFieldGetter(String line, String delimiter) {
    fields = line.split(delimiter);
  }

  public String getFieldValue() {
    String value;
    try {
      value = fields[index];
      index++;
    } catch (Exception e) {
      value = null;
    }
    return value;
  }

  public int getFieldValueAsInteger() {
    return getFieldValueAsInteger(0);
  }

  public int getFieldValueAsInteger(int defaultValue) {
    int value;
    try {
      String v = fields[index];
      index++;
      value = Integer.parseInt(v);
    } catch (Exception e) {
      value = defaultValue;
    }
    return value;
  }

  public long getFieldValueAsLong() {
    return getFieldValueAsLong(0L);
  }

  public long getFieldValueAsLong(long defaultValue) {
    long value;
    try {
      String v = fields[index];
      index++;
      value = Long.parseLong(v);
    } catch (Exception e) {
      value = defaultValue;
    }
    return value;
  }

  public boolean getFieldValueAsBoolean(String valueAsTrue) {
    return getFieldValueAsBoolean(valueAsTrue, false);
  }

  public boolean getFieldValueAsBoolean(String valueAsTrue, boolean caseSensitive) {
    try {
      String s = fields[index];
      index++;
      if (!caseSensitive) {
        valueAsTrue.toLowerCase();
        s.toLowerCase();
      }
      if (s.equals(valueAsTrue)) {
        return true;
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }

}
