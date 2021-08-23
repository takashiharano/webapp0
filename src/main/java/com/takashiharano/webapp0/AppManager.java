package com.takashiharano.webapp0;

import com.libutil.FileUtil;
import com.libutil.Props;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.task.HeapMonitor;
import com.takashiharano.webapp0.task.IntervalTask;
import com.takashiharano.webapp0.task.IntervalTaskManager;
import com.takashiharano.webapp0.util.Log;

public class AppManager {

  private static final String APPHOME_BASENAME = "webapphome";
  private static final String PROPERTIES_FILENAME = "app.properties";
  private static final String CONFIGKEY_WORKSPACE = "app_workspace";

  private static AppManager instance;
  private static Props config;
  private static String errorInfo;
  private static String appHomePath;
  private static String appWorkspacePath;
  private IntervalTaskManager intervalTaskManager;
  private static AsyncTaskManager asyncTaskManager;

  private AppManager() {
    intervalTaskManager = new IntervalTaskManager();
  }

  public static AppManager getInstance() {
    if (instance == null) {
      instance = new AppManager();
    }
    return instance;
  }

  public static void onStart() {
    init();
  }

  public static void onStop() {
    getInstance().stopIntervalTsak();
    Log.i("[OK] ==> APP STOPPED");
  }

  public static String getBasePackageName() {
    return AppInfo.BASE_PACKAGE_NAME;
  }

  public static String getModuleName() {
    return AppInfo.MODULE_NAME;
  }

  public static void reset() {
    Log.i("Resetting app...");
    init();
  }

  public static boolean isReady() {
    return errorInfo == null;
  }

  public static String getErrorInfo() {
    return errorInfo;
  }

  public static String getAppHomePath() {
    return appHomePath;
  }

  public static String getAppWorkspacePath() {
    return appWorkspacePath;
  }

  private static void init() {
    errorInfo = null;
    try {
      _init();
      Log.i("[OK] ==> APP READY");
    } catch (Exception e) {
      errorInfo = e.getMessage();
      Log.i("[NG] ==> APP INIT ERROR : " + errorInfo);
    }
  }

  private static void _init() throws Exception {
    String homePath = System.getenv("HOME");
    if (homePath == null) {
      throw new Exception("System env \"HOME\" is not defined.");
    }
    int logLevel = Log.LogLevel.DEBUG.getLevel();
    Log.setup(logLevel, AppInfo.MODULE_NAME);
    appHomePath = homePath + "/" + APPHOME_BASENAME + "/" + AppInfo.MODULE_NAME;
    Log.i("WebAppHome: " + appHomePath);

    String propFilePath = appHomePath + "/" + PROPERTIES_FILENAME;
    if (FileUtil.notExist(propFilePath)) {
      throw new Exception("App config not found: path=" + propFilePath);
    }
    config = new Props(propFilePath);

    appWorkspacePath = config.getValue(CONFIGKEY_WORKSPACE);
    if (StrUtil.isEmpty(appWorkspacePath)) {
      appWorkspacePath = appHomePath;
    }
    Log.i("WebAppWorkspace: " + appWorkspacePath);

    getInstance().startIntervalTask();
    asyncTaskManager = AsyncTaskManager.getInstance();
  }

  public static String getConfigValue(String key) {
    return config.getValue(key);
  }

  public static String getConfigValue(String key, String defaultValue) {
    return config.getValue(key);
  }

  public static int getConfigIntValue(String key) {
    return config.getIntValue(key);
  }

  public static float getConfigFloatValue(String key) {
    return config.getFloatValue(key);
  }

  public static double getConfigDoubleValue(String key) {
    return config.getDoubleValue(key);
  }

  public static boolean getConfigBooleanValue(String key) {
    return config.getBooleanValue(key);
  }

  public AsyncTaskManager getAsyncTaskManager() {
    return asyncTaskManager;
  }

  private void startIntervalTask() {
    stopIntervalTsak();
    IntervalTask task = new HeapMonitor();
    intervalTaskManager.startTask("heapmon", task, 180);
  }

  private void stopIntervalTsak() {
    intervalTaskManager.stopAllTasks();
  }

}
