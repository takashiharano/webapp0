package com.takashiharano.webapp0.util;

import com.libutil._Log;

public class Log extends _Log {

  public static void setup(int level, String moduleName) {
    instance = new Log();
    Log.setLevel(level);
    Log.setModuleName(moduleName);
  }

}
