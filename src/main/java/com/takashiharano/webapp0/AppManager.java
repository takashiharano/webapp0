/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
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

  /**
   * Returns the instance of the AppManager.
   *
   * @return AppManager
   */
  public static AppManager getInstance() {
    if (instance == null) {
      instance = new AppManager();
    }
    return instance;
  }

  /**
   * Callback for the web application initialization.<br>
   * This is called by ServletContextListener#contextInitialized().
   */
  public void onStart() {
    init();
  }

  /**
   * Callback for the web application shut down. This is called by
   * ServletContextListener#contextDestroyed().
   */
  public void onStop() {
    getInstance().stopIntervalTsaks();
    sessionManager.onStop();
    Log.i("[OK] ==> APP STOPPED");
  }

  public static String getBasePackageName() {
    return BASE_PACKAGE_NAME;
  }

  public static String getModuleName() {
    return MODULE_NAME;
  }

  /**
   * Reload the properties and re-initialize the application.
   */
  public void reset() {
    Log.i("Resetting app...");
    init();
  }

  /**
   * Returns whether the web application can work normally.<br>
   * Returns false if any error occurred during initialization.
   *
   * @return true if the application can work normally.
   */
  public boolean isReady() {
    return errorInfo == null;
  }

  /**
   * Returns details of any errors that occurred during initialization.
   *
   * @return the details of the errors. null if no error.
   */
  public String getErrorInfo() {
    return errorInfo;
  }

  /**
   * Returns the application home path.<br>
   * Generally it will be /home/USER/webapphome/MODULE on linux or<br>
   * C:/Users/USER/webapphome/MODULE on Windows.
   *
   * @return the path
   */
  public String getAppHomePath() {
    return appHomePath;
  }

  /**
   * Returns the workspace path for the application defined in app.properties with
   * "workspace" key.<br>
   * If there is no definition, returns the same path as the app home.
   *
   * @return the workspace path
   */
  public String getAppWorkspacePath() {
    return appWorkspacePath;
  }

  /**
   * The implementation of the initialization.
   */
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

    userManager = UserManager.getInstance();
    userManager.init();

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
    startIntervalTasks();
    asyncTaskManager = AsyncTaskManager.getInstance();
  }

  /**
   * Returns the configuration object that loaded from app.properties.
   *
   * @return the configuration object
   */
  public Props getConfig() {
    return config;
  }

  /**
   * Returns the property value corresponding the specified key.<br>
   * If the key is not found in the properties file, returns null.
   *
   * @param key
   *          the key
   * @return the value
   */
  public String getConfigValue(String key) {
    return config.getValue(key);
  }

  /**
   * Returns the property value corresponding the specified key.
   * 
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public String getConfigValue(String key, String defaultValue) {
    return config.getValue(key);
  }

  /**
   * Returns the property value as a boolean corresponding the specified key.
   *
   * @param key
   *          the key
   * @return A zero value, "false", "", null, are converted to false; any other
   *         value is converted to true. The value is case-insensitive.
   */
  public boolean getConfigValueAsBoolean(String key) {
    return config.getValueAsBoolean(key);
  }

  /**
   * Returns the property value as a boolean corresponding the specified key.
   *
   * @param key
   *          the key
   * @param valueAsTrue
   *          the value to be true
   * @return true if the value in this property list with the specified key value
   *         equals valueAsTrue.
   */
  public boolean getConfigValueAsBoolean(String key, String valueAsTrue) {
    return config.getValueAsBoolean(key, valueAsTrue);
  }

  /**
   * Returns the property value as a double corresponding the specified key.<br>
   * If the key is not found in the properties file, returns 0.0.
   *
   * @param key
   *          the key
   * @return the value
   */
  public double getConfigValueAsDouble(String key) {
    return config.getValueAsDouble(key);
  }

  /**
   * Returns the property value as a double corresponding the specified key.
   * 
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public double getConfigValueAsDouble(String key, double defaultValue) {
    return config.getValueAsDouble(key, defaultValue);
  }

  /**
   * Returns the property value as a float corresponding the specified key.<br>
   * If the key is not found in the properties file, returns 0.0f.
   *
   * @param key
   *          the key
   * @return the value
   */
  public float getConfigValueAsFloat(String key) {
    return config.getValueAsFloat(key);
  }

  /**
   * Returns the property value as a float corresponding the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public float getConfigValueAsFloat(String key, float defaultValue) {
    return config.getValueAsFloat(key, defaultValue);
  }

  /**
   * Returns the property value as an integer corresponding the specified key.<br>
   * If the key is not found in the properties file, returns 0.
   *
   * @param key
   *          the value
   * @return the value
   */
  public int getConfigValueAsInteger(String key) {
    return config.getValueAsInteger(key);
  }

  /**
   * Returns the property value as an integer corresponding the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public int getConfigValueAsInteger(String key, int defaultValue) {
    return config.getValueAsInteger(key, defaultValue);
  }

  /**
   * Returns the property value as a long corresponding the specified key.<br>
   * If the key is not found in the properties file, returns 0.
   *
   * @param key
   *          the value
   * @return the value
   */
  public long getConfigValueAsLong(String key) {
    return config.getValueAsLong(key);
  }

  /**
   * Returns the property value as a long corresponding the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public long getConfigValueAsLong(String key, long defaultValue) {
    return config.getValueAsLong(key, defaultValue);
  }

  /**
   * Returns the session manager object.
   *
   * @return SessionManager
   */
  public SessionManager getSessionManager() {
    return sessionManager;
  }

  /**
   * Returns the user manager object.
   *
   * @return UserManager
   */
  public UserManager getUserManager() {
    return userManager;
  }

  /**
   * Returns the async task manager object.
   *
   * @return AsyncTaskManager
   */
  public AsyncTaskManager getAsyncTaskManager() {
    return asyncTaskManager;
  }

  /**
   * Starts the interval tasks.
   */
  private void startIntervalTasks() {
    stopIntervalTsaks();

    // Sample implementation of the Interval Task.
    // TODO Remove if not necessary
    IntervalTask task = new HeapMonitor();
    intervalTaskManager.startTask("heapmon", task, 180);
  }

  /**
   * Stops all interval tasks.
   */
  private void stopIntervalTsaks() {
    intervalTaskManager.stopAllTasks();
  }

}
