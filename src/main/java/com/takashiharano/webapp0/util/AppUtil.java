/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.util;

import java.util.Set;

public class AppUtil {

  public static String convertSetToOneLineString(Set<String> items, String separator) {
    StringBuilder sb = new StringBuilder();
    int cnt = 0;
    for (String item : items) {
      if (cnt > 0) {
        sb.append(separator);
      }
      sb.append(item);
      cnt++;
    }
    return sb.toString();
  }

  public static String getFieldValue(String[] values, int index) {
    String value;
    try {
      value = values[index];
    } catch (Exception e) {
      value = null;
    }
    return value;
  }

  public static int getFieldValueAsInteger(String[] values, int index) {
    return getFieldValueAsInteger(values, index, 0);
  }

  public static int getFieldValueAsInteger(String[] values, int index, int defaultValue) {
    int value;
    try {
      String v = values[index];
      value = Integer.parseInt(v);
    } catch (Exception e) {
      value = defaultValue;
    }
    return value;
  }

  public static long getFieldValueAsLong(String[] values, int index) {
    return getFieldValueAsLong(values, index, 0L);
  }

  public static long getFieldValueAsLong(String[] values, int index, long defaultValue) {
    long value;
    try {
      String v = values[index];
      value = Long.parseLong(v);
    } catch (Exception e) {
      value = defaultValue;
    }
    return value;
  }

  public static boolean getFieldValueAsBoolean(String[] values, int index, String valueAsTrue) {
    return getFieldValueAsBoolean(values, index, valueAsTrue, false);
  }

  public static boolean getFieldValueAsBoolean(String[] values, int index, String valueAsTrue, boolean caseSensitive) {
    try {
      String s = values[index];
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
