package com.takashiharano.webapp0;

import com.libutil.FileUtil;
import com.libutil.Props;
import com.libutil.StrUtil;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.task.HeapMonitor;
import com.takashiharano.webapp0.task.IntervalTask;
import com.takashiharano.webapp0.task.IntervalTaskManager;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

public class AppManager {

  public static final String BASE_PACKAGE_NAME = "com.takashiharano.webapp0";
  public static final String MODULE_NAME = "webapp0";

  private static final String APPHOME_BASENAME = "webapphome";
  private static final String PROPERTIES_FILENAME = "app.properties";
  private static final String CONFIGKEY_WORKSPACE = "workspace";

  private static AppManager instance;
  private String appHomePath;
  private String appWorkspacePath;
  private Props config;
  private String errorInfo;
  private UserManager userManager;
  private SessionManager sessionManager;
  private IntervalTaskManager intervalTaskManager;
  private AsyncTaskManager asyncTaskManager;

  public static AppManager getInstance() {
    if (instance == null) {
      instance = new AppManager();
    }
    return instance;
  }

  public void onStart() {
    init();
  }

  public void onStop() {
    getInstance().stopIntervalTsak();
    sessionManager.onStop();
    Log.i("[OK] ==> APP STOPPED");
  }

  public static String getBasePackageName() {
    return BASE_PACKAGE_NAME;
  }

  public static String getModuleName() {
    return MODULE_NAME;
  }

  public void reset() {
    Log.i("Resetting app...");
    init();
  }

  public boolean isReady() {
    return errorInfo == null;
  }

  public String getErrorInfo() {
    return errorInfo;
  }

  public String getAppHomePath() {
    return appHomePath;
  }

  public String getAppWorkspacePath() {
    return appWorkspacePath;
  }

  private void init() {
    errorInfo = null;
    try {
      _init();
      Log.i("[OK] ==> APP READY");
    } catch (Exception e) {
      Log.e(e);
      errorInfo = e.toString();
      Log.i("[NG] ==> APP INIT ERROR : " + errorInfo);
    }
  }

  private void _init() throws Exception {
    String homePath = System.getenv("HOME");
    if (homePath == null) {
      throw new Exception("System env \"HOME\" is not defined.");
    }
    int logLevel = Log.LogLevel.DEBUG.getLevel();
    Log.setup(logLevel, MODULE_NAME);
    appHomePath = homePath + "/" + APPHOME_BASENAME + "/" + MODULE_NAME;
    Log.i("WebAppHome: " + appHomePath);

    String propFilePath = appHomePath + "/" + PROPERTIES_FILENAME;
    if (!FileUtil.exists(propFilePath)) {
      throw new Exception("App config not found: path=" + propFilePath);
    }
    config = new Props(propFilePath);

    appWorkspacePath = config.getValue(CONFIGKEY_WORKSPACE);
    if (StrUtil.isEmpty(appWorkspacePath)) {
      appWorkspacePath = appHomePath;
    }
    Log.i("WebAppWorkspace: " + appWorkspacePath);

    userManager = new UserManager();

    if (sessionManager == null) {
      String sessionPath = FileUtil.joinPath(getAppWorkspacePath(), "sessions.txt");
      sessionManager = new SessionManager(sessionPath);
      if (FileUtil.exists(sessionPath)) {
        sessionManager.loadSessionInfo(sessionPath);
      }
    }

    if (intervalTaskManager == null) {
      intervalTaskManager = new IntervalTaskManager();
    }
    startIntervalTask();
    asyncTaskManager = AsyncTaskManager.getInstance();
  }

  public Props getConfig() {
    return config;
  }

  public String getConfigValue(String key) {
    return config.getValue(key);
  }

  public String getConfigValue(String key, String defaultValue) {
    return config.getValue(key);
  }

  public int getConfigIntValue(String key) {
    return config.getIntValue(key);
  }

  public int getConfigIntValue(String key, int defaultValue) {
    return config.getIntValue(key, defaultValue);
  }

  public float getConfigFloatValue(String key) {
    return config.getFloatValue(key);
  }

  public float getConfigFloatValue(String key, float defaultValue) {
    return config.getFloatValue(key, defaultValue);
  }

  public double getConfigDoubleValue(String key) {
    return config.getDoubleValue(key);
  }

  public double getConfigDoubleValue(String key, double defaultValue) {
    return config.getDoubleValue(key, defaultValue);
  }

  public boolean isConfigTrue(String key) {
    return config.isTrue(key);
  }

  public SessionManager getSessionManager() {
    return sessionManager;
  }

  public UserManager getUserManager() {
    return userManager;
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
