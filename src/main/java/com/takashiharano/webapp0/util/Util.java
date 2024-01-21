/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2024 Takashi Harano
 */
package com.takashiharano.webapp0.util;

import java.util.Set;

import com.libutil.StrUtil;

public class Util {

  public static String snipSessionId(String sessionId) {
    String s = StrUtil.snip(sessionId, 7, 2);
    return s;
  }

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

}
