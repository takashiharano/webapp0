/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bsb64.BSB64;
import com.libutil.Base64Util;
import com.libutil.FileUtil;
import com.libutil.JsonBuilder;
import com.libutil.Props;
import com.takashiharano.webapp0.async.AsyncTaskManager;
import com.takashiharano.webapp0.session.SessionInfo;
import com.takashiharano.webapp0.session.SessionManager;
import com.takashiharano.webapp0.user.GroupManager;
import com.takashiharano.webapp0.user.User;
import com.takashiharano.webapp0.user.UserManager;
import com.takashiharano.webapp0.util.Log;

/**
 * The context of the request processing.
 */
public class ProcessContext {
  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletContext servletContext;
  private String actionName;
  private String responseType;
  private String remoteAddr;
  private String remoteHost;
  private String xForwardedFor;
  private String userAgent;
  private String localAddr;
  private String localHostName;
  private HttpSession httpSession;
  private Cookie[] cookies;
  private HashMap<String, Object> info;

  public ProcessContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    try {
      request.setCharacterEncoding("UTF-8");
    } catch (UnsupportedEncodingException e) {
      Log.e(e);
    }
    this.request = request;
    this.response = response;
    this.servletContext = servletContext;
    this.info = new HashMap<>();
    this.actionName = request.getParameter("action");
    this.remoteAddr = request.getRemoteAddr();
    this.remoteHost = request.getRemoteHost();
    this.xForwardedFor = request.getHeader("X-Forwarded-For");
    this.userAgent = request.getHeader("User-Agent");
    this.localHostName = request.getLocalName();
    this.localAddr = request.getLocalAddr();
    this.httpSession = request.getSession();
    this.cookies = request.getCookies();
    request.setAttribute("context", this);
  }

  /**
   * Returns the HttpServletRequestt object of the current request.
   *
   * @return HttpServletRequest
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * Returns the HttpServletResponse object of the current request.
   *
   * @return HttpServletResponse
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * Returns the ServletContext object of the current request.
   *
   * @return ServletContext
   */
  public ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * Returns the request parameter of the specified key.
   *
   * @param key
   *          the key
   * @return the value or null if the parameter does not exist.
   */
  public String getRequestParameter(String key) {
    return request.getParameter(key);
  }

  /**
   * Returns the request parameter as an integer corresponding to the specified
   * key.
   *
   * @param key
   *          the key
   * @return the value or 0 if the parameter does not exist.
   */
  public int getRequestParameterAsInteger(String key) {
    return getRequestParameterAsInteger(key, 0);
  }

  /**
   * Returns the request parameter as an integer corresponding to the specified
   * key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public int getRequestParameterAsInteger(String key, int defaultValue) {
    String s = request.getParameter(key);
    int v;
    try {
      v = Integer.parseInt(s);
    } catch (Exception e) {
      v = defaultValue;
    }
    return v;
  }

  /**
   * Returns the request parameter as a long corresponding to the specified key.
   *
   * @param key
   *          the key
   * @return the value or 0L if the parameter does not exist.
   */
  public long getRequestParameterAsLong(String key) {
    return getRequestParameterAsLong(key, 0L);
  }

  /**
   * Returns the request parameter as a long corresponding to the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public long getRequestParameterAsLong(String key, long defaultValue) {
    String s = request.getParameter(key);
    long v;
    try {
      v = Long.parseLong(s);
    } catch (Exception e) {
      v = defaultValue;
    }
    return v;
  }

  /**
   * Returns the request parameter as a float corresponding to the specified key.
   *
   * @param key
   *          the key
   * @return the value or 0.0f the parameter does not exist.
   */
  public float getRequestParameterAsFloat(String key) {
    return getRequestParameterAsFloat(key, 0);
  }

  /**
   * Returns the request parameter as a float corresponding to the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public float getRequestParameterAsFloat(String key, float defaultValue) {
    String s = request.getParameter(key);
    float v;
    try {
      v = Float.parseFloat(s);
    } catch (Exception e) {
      v = defaultValue;
    }
    return v;
  }

  /**
   * Returns the request parameter as a double corresponding to the specified key.
   *
   * @param key
   *          the key
   * @return the value or 0.0 the parameter does not exist.
   */
  public double getRequestParameterAsDouble(String key) {
    return getRequestParameterAsFloat(key, 0);
  }

  /**
   * Returns the request parameter as a double corresponding to the specified key.
   *
   * @param key
   *          the key
   * @param defaultValue
   *          the default value in case of the key not found
   * @return the value
   */
  public double getRequestParameterAsDouble(String key, double defaultValue) {
    String s = request.getParameter(key);
    double v;
    try {
      v = Double.parseDouble(s);
    } catch (Exception e) {
      v = defaultValue;
    }
    return v;
  }

  /**
   * Returns the request parameter as a boolean corresponding to the specified
   * key.
   *
   * @param key
   *          the key
   * @param trueValue
   *          the value to be true
   * @return true or false
   */
  public boolean getRequestParameterAsBoolean(String key, String trueValue) {
    return getRequestParameterAsBoolean(key, trueValue, false);
  }

  /**
   * Returns the request parameter as a boolean corresponding to the specified
   * key.
   *
   * @param key
   *          the key
   * @param trueValue
   *          the value to be true
   * @param caseIgnore
   *          true if ignore the case
   * @return trur oe false
   */
  public boolean getRequestParameterAsBoolean(String key, String trueValue, boolean caseIgnore) {
    String s = request.getParameter(key);
    if (s == null) {
      return false;
    }
    if (caseIgnore) {
      trueValue.toLowerCase();
      s.toLowerCase();
    }
    if (s.equals(trueValue)) {
      return true;
    }
    return false;
  }

  /**
   * Returns the URL-decoded request parameters.
   *
   * @param key
   *          the key
   * @return the URL-decoded value
   */
  public String getUrlDecodedRequestParameter(String key) {
    String value = getRequestParameter(key);
    String decodedValue = null;
    try {
      decodedValue = URLDecoder.decode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return decodedValue;
  }

  /**
   * Returns the Base64-decoded request parameters.
   *
   * @param key
   *          the key
   * @return the Base64-decoded value
   */
  public String getBase64DecodedRequestParameter(String key) {
    String value = getRequestParameter(key);
    if (value == null) {
      return null;
    }
    return Base64Util.decodeToString(value);
  }

  /**
   * Returns the BSB64-decoded request parameters.<br>
   * The number of shifts is used the value set in properties with
   * "bab64_n_param".
   *
   * @param key
   *          the key
   * @return the BSB64-decoded value
   */
  public String getBSB64DecodedRequestParameter(String key) {
    String value = getRequestParameter(key);
    if (value == null) {
      return null;
    }
    int n = getConfigValueAsInteger("bab64_n_param", 1);
    return BSB64.decodeString(value, n);
  }

  /**
   * Returns the Cookie value for the given name.
   *
   * @param name
   *          field name
   * @return Cookie value. null if not found.
   */
  public String getCookie(String name) {
    String value = null;
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          value = cookie.getValue();
          break;
        }
      }
    }
    return value;
  }

  /**
   * Forwards a request from a servlet to another resource.
   *
   * @param path
   *          path to forward
   * @throws IOException
   *           If an I/O error occurs
   * @throws ServletException
   *           If a Servlet error occurs
   */
  public void forward(String path) throws IOException, ServletException {
    request.getRequestDispatcher(path).forward(request, response);
  }

  /**
   * Returns an action name for the current process.
   *
   * @return the action name
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * Sets the action name for the current process.
   *
   * @param actionName
   *          the action name
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  /**
   * Sets HTTP response code for the current request.
   *
   * @param status
   *          the status code
   */
  public void setResponseCode(int status) {
    response.setStatus(status);
  }

  /**
   * Sets an HTTP response header for the current request.
   *
   * @param name
   *          the field name
   * @param value
   *          the value for the field
   */
  public void setResponseHeader(String name, String value) {
    response.setHeader(name, value);
  }

  /**
   * Sends the plain text response to the client.
   *
   * @param text
   *          the text to be sent
   */
  public void sendTextResponse(String text) {
    sendResponse("text/plain", text);
  }

  /**
   * Sends a JSON response to the client.
   *
   * @param json
   *          the JSON text to be sent
   */
  public void sendJsonResponse(String json) {
    sendResponse("application/json", json);
  }

  /**
   * Sends a JSON response to the client.
   *
   * @param status
   *          the status field in JSON
   * @param body
   *          the body field in JSON
   */
  public void sendJsonResponse(String status, String body) {
    sendJsonResponse(status, body, true);
  }

  /**
   * Sends a JSON response to the client.
   *
   * @param jb
   *          the JsonBuilder object
   */
  public void sendJsonResponse(JsonBuilder jb) {
    sendJsonResponse(jb.toString());
  }

  /**
   * Sends a JSON response to the client.
   * 
   * @param status
   *          the status field in JSON
   * @param body
   *          the body field in JSON
   * @param isObject
   *          true if the body is an object format
   */
  public void sendJsonResponse(String status, String body, boolean isObject) {
    JsonBuilder jb = new JsonBuilder();
    jb.append("status", status);
    if (isObject) {
      jb.appendObject("body", body);
    } else {
      jb.append("body", body);
    }
    sendJsonResponse(jb.toString());
  }

  /**
   * Sends a response.
   *
   * @param contentType
   *          the Content-Type field value
   * @param text
   *          the string to be sent
   */
  public void sendResponse(String contentType, String text) {
    response.setContentType(contentType + ";charset=utf-8");
    try (PrintWriter writer = response.getWriter()) {
      writer.write(text);
    } catch (IOException e) {
      Log.e(e);
    }
  }

  /**
   * Sends a response with byte-stream to the client.
   *
   * @param b
   *          the byte array to be sent
   * @param fileName
   *          the file name for the Content-Disposition field
   * @throws IOException
   *           If an IO error occurs
   */
  public void sendByteStreamResponse(byte[] b, String fileName) throws IOException {
    setStreamResponseHeader(fileName);
    sendByteArray(b);
  }

  /**
   * Sends a response with a text as a byte-stream to the client.
   *
   * @param text
   *          the text to be sent
   * @param fileName
   *          the file name for the Content-Disposition field
   * @throws IOException
   *           If an IO error occurs
   */
  public void sendTextStreamResponse(String text, String fileName) throws IOException {
    setStreamResponseHeader(fileName);
    byte[] b = text.getBytes("UTF-8");
    sendByteArray(b);
  }

  /**
   * Sends a response with a file as a byte-stream to the client.
   *
   * @param path
   *          the file path
   * @throws IOException
   *           If an IO error occurs
   */
  public void sendFileResponse(String path) throws IOException {
    sendFileResponse(path, null);
  }

  /**
   * Sends a response with a file as a byte-stream to the client.
   * 
   * @param path
   *          the file path
   * @param fileName
   *          the file name for the Content-Disposition field
   * @throws IOException
   *           If an IO error occurs
   */
  public void sendFileResponse(String path, String fileName) throws IOException {
    if (fileName == null) {
      fileName = FileUtil.getFileName(path);
    }
    setStreamResponseHeader(fileName);
    sendFile(path);
  }

  private void setStreamResponseHeader(String fileName) {
    response.setContentType("application/octet-stream");
    String encodedFileName = null;
    try {
      encodedFileName = URLEncoder.encode(fileName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // unreachable
    }
    String contentDisposition = "attachment;filename=\"" + fileName + "\";filename*=utf-8''" + encodedFileName;
    response.setHeader("Content-Disposition", contentDisposition);
  }

  private void sendByteArray(byte[] b) throws IOException {
    try (ServletOutputStream os = response.getOutputStream()) {
      os.write(b);
    } catch (IOException ioe) {
      throw ioe;
    }
  }

  private void sendFile(String path) throws IOException {
    try (ServletOutputStream os = response.getOutputStream(); FileInputStream fis = new FileInputStream(path); BufferedInputStream bis = new BufferedInputStream(fis); DataInputStream dis = new DataInputStream(bis);) {

      byte[] b = new byte[1 * 1024 * 1024];
      int readSize = 0;

      while (-1 != (readSize = dis.read(b))) {
        os.write(b, 0, readSize);
      }
    } catch (IOException ioe) {
      throw ioe;
    }
  }

  /**
   * Returns the response type.
   *
   * @return the response type
   */
  public String getResponseType() {
    return responseType;
  }

  /**
   * Sets a response type.
   *
   * @param responseType
   *          The response type. Acceptable values are "text", "html", "json".
   */
  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  /**
   * Returns a value for general information.
   *
   * @param key
   *          the key
   * @return the value
   */
  public Object getInfo(String key) {
    return info.get(key);
  }

  /**
   * Sets a value for general information.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void setInfo(String key, Object value) {
    info.put(key, value);
  }

  /**
   * Returns the accessed IP address.
   *
   * @return the IP address
   */
  public String getRemoteAddr() {
    return remoteAddr;
  }

  /**
   * Returns the accessed host name.
   *
   * @return the host name
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * Returns the X-Forwarded-For header value.
   *
   * @return X-Forwarded-For header value
   */
  public String getXForwardedFor() {
    return xForwardedFor;
  }

  /**
   * Returns the accessed address.
   *
   * @param byName
   *          if true and the host name is available, returns the host name
   *          instead of the IP address.
   * @return address
   */
  public String getRemoteAddress(boolean byName) {
    String addr = getXForwardedFor();
    if (addr != null) {
      return addr;
    }

    if (byName) {
      addr = getRemoteHost();
      if (addr != null) {
        return addr;
      }
    }

    addr = getRemoteAddr();
    return addr;
  }

  /**
   * Returns User-Agent HTTP request header.
   *
   * @return User-Agent field value
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Returns the IP address of the server.
   *
   * @return the IP address
   */
  public String getLocalAddr() {
    return localAddr;
  }

  /**
   * Returns the host name of the server.
   *
   * @return the host name
   */
  public String getLocaHostlName() {
    return localHostName;
  }

  /**
   * Return HTTP session object.
   *
   * @return Servlet HttpSession
   */
  public HttpSession getHttpSession() {
    return httpSession;
  }

  /**
   * Returns current session id.
   *
   * @return Session id
   */
  public String getSessionId() {
    return getCookie(SessionManager.SESSION_COOKIE_NAME);
  }

  /**
   * Returns current session info.
   *
   * @return Session info
   */
  public SessionInfo getSessionInfo() {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return null;
    }
    AppManager appManager = getAppManager();
    SessionManager sessionManager = appManager.getSessionManager();
    SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
    return sessionInfo;
  }

  /**
   * Returns current user info.
   *
   * @return User info
   */
  public User getUserInfo() {
    SessionInfo sessionInfo = getSessionInfo();
    if (sessionInfo == null) {
      return null;
    }

    String username = sessionInfo.getUsername();

    UserManager userManager = getUserManager();
    User userInfo = userManager.getUserInfo(username);
    return userInfo;
  }

  /**
   * Returns requested URI (/xxxx/main?aaa=bbb)
   *
   * @return URI
   */
  public String getRequestedUri() {
    String requestUri = request.getRequestURI();
    String queryString = request.getQueryString();
    String requestedUri = requestUri;
    if (queryString != null) {
      requestedUri += "?" + queryString;
    }
    return requestedUri;
  }

  /**
   * Returns current username.
   *
   * @return username
   */
  public String getUsername() {
    return getUsername("");
  }

  /**
   * Returns current username.
   *
   * @param defaultName
   *          Default name if no information is available
   * @return username
   */
  public String getUsername(String defaultName) {
    String username = defaultName;
    User userInfo = getUserInfo();
    if (userInfo != null) {
      username = userInfo.getUsername();
    }
    return username;
  }

  /**
   * Returns current user full name.
   *
   * @return user full name
   */
  public String getUserFullName() {
    String name = "";
    User userInfo = getUserInfo();
    if (userInfo != null) {
      name = userInfo.getFullName();
    }
    return name;
  }

  /**
   * Returns current user local full name.
   *
   * @return user local full name
   */
  public String getUserLocalFullName() {
    String name = "";
    User userInfo = getUserInfo();
    if (userInfo != null) {
      name = userInfo.getLocalFullName();
    }
    return name;
  }

  /**
   * Returns if the current user is administrator.
   *
   * @return true if the user is administrator; false otherwise
   */
  public boolean isAdmin() {
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return false;
    }
    return userInfo.isAdmin();
  }

  /**
   * Returns the groups for the current user in an array of the string.
   *
   * @return the groups list
   */
  public String[] getGroups() {
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return null;
    }
    return userInfo.getGroups();
  }

  /**
   * Returns whether the user belongs to the group.
   *
   * @param group
   *          target group name
   * @return true if the user belongs to the group
   */
  public boolean isBelongToGroup(String group) {
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return false;
    }
    return userInfo.isBelongToGroup(group);
  }

  /**
   * Returns the privileges for the current user in an array of the string.
   *
   * @return the privileges list
   */
  public String[] getPrivileges() {
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return null;
    }
    return userInfo.getPrivileges();
  }

  /**
   * Returns whether the current user has the specified privilege.<br>
   * True if the user or a group to which the user belongs has the privilege.
   *
   * @param privilege
   *          the privilege to check
   * @return true if the user has the privilege. always true if the user is admin.
   */
  public boolean hasPermission(String privilege) {
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return false;
    }
    return userInfo.hasPermission(privilege);
  }

  /**
   * Sends the error screen to the client.
   *
   * @param errorInfo
   *          an error information string
   * @throws ServletException
   *           If a Servlet error occurs
   * @throws IOException
   *           If an IO error occurs
   */
  public void sendErrorScreen(String errorInfo) throws ServletException, IOException {
    setInfo("errorInfo", errorInfo);
    String path = "error.jsp";
    Log.i("Screen => " + path);
    forward(path);
  }

  /**
   * Returns the application home path.<br>
   * Generally it will be /home/USER/webapphome/MODULE on linux or<br>
   * C:/Users/USER/webapphome/MODULE on Windows.
   *
   * @return the path
   */
  public String getAppHomePath() {
    AppManager appManager = getAppManager();
    String path = appManager.getAppHomePath();
    return path;
  }

  /**
   * Returns the workspace path for the application defined in app.properties with
   * "workspace" key.<br>
   * If there is no definition, returns the same path as the app home.
   *
   * @return the workspace path
   */
  public String getAppWorkspacePath() {
    AppManager appManager = getAppManager();
    String path = appManager.getAppWorkspacePath();
    return path;
  }

  /**
   * Returns the configuration object.
   *
   * @return Props
   */
  public Props getConfig() {
    AppManager appManager = getAppManager();
    Props config = appManager.getConfig();
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
    return getConfigValue(key, null);
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
    Props config = getConfig();
    return config.getValue(key);
  }

  /**
   * Returns true if the value is not null, "false", "0", "".
   *
   * @param key
   *          the key
   * @return false if the value is null, "false", "0", ""; otherwise true
   */
  public boolean getConfigValueAsBoolean(String key) {
    Props config = getConfig();
    return config.getValueAsBoolean(key);
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
    return getConfigValueAsDouble(key, 0);
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
    Props config = getConfig();
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
    return getConfigValueAsFloat(key, 0f);
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
    Props config = getConfig();
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
    return getConfigValueAsInteger(key, 0);
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
    Props config = getConfig();
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
    return getConfigValueAsLong(key, 0);
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
    Props config = getConfig();
    return config.getValueAsLong(key, defaultValue);
  }

  /**
   * Returns if the specified key exists in the app configuration.
   *
   * @param key
   *          the key
   * @return true if the key is valid; false otherwise
   */
  public boolean hasConfigKey(String key) {
    Props config = getConfig();
    return config.containsKey(key);
  }

  /**
   * Returns if the value for the specified key exists.
   *
   * @param key
   *          the key for the value
   * @return true if the key is valid and the corresponding value is not empty;
   *         false otherwise
   */
  public boolean hasConfigValue(String key) {
    Props config = getConfig();
    return config.hasValue(key);
  }

  /**
   * Dump the request parameters.
   *
   * @return all request parameters
   */
  public String dumpParameters() {
    Map<String, String[]> params = request.getParameterMap();
    StringBuilder sb = new StringBuilder();
    int paramCount = 0;

    for (Map.Entry<String, String[]> entry : params.entrySet()) {
      paramCount++;
      String key = entry.getKey();
      String[] values = entry.getValue();

      if (paramCount > 1) {
        sb.append("&");
      }
      sb.append(key);

      if ((params.size() > 1) || (!"".equals(values[0]))) {
        sb.append("=");
      }

      int valCount = 0;
      for (int i = 0; i < values.length; i++) {
        valCount++;
        if (valCount > 1) {
          sb.append(",");
        }
        sb.append(values[i]);
      }
    }

    return sb.toString();
  }

  /**
   * Returns the value for the given name from MANIFEST.MF.
   *
   * @param name
   *          field name
   * @return the value
   * @throws IOException
   *           If an IO error occurs
   */
  public String getManifestEntry(String name) throws IOException {
    String value = null;
    try (InputStream is = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF")) {
      Manifest manifest = new Manifest(is);
      Attributes attributes = manifest.getMainAttributes();
      value = attributes.getValue(name);
    } catch (IOException ioe) {
      throw ioe;
    }
    return value;
  }

  /**
   * Returns APP version
   *
   * @return APP version
   */
  public String getAppVersion() {
    String version;
    try {
      version = getManifestEntry("App-Version");
    } catch (IOException ioe) {
      version = "";
    }
    return version;
  }

  /**
   * The process should be always called first when received a request.
   */
  public void onAccess() {
    Log.setContext(this);
    long timestamp = System.currentTimeMillis();
    setSessionCookieMaxAge();
    SessionManager sessionManager = getSessionManager();
    sessionManager.onAccess(this, timestamp);
    UserManager userManager = getUserManager();
    userManager.onAccess(this, timestamp);
  }

  /**
   * The process should be always called at the end of request processing.
   */
  public void onAccessEnd() {
    Log.removeContext();
  }

  /**
   * Returns if the context has a valid session.
   *
   * @return true if the context has a valid session
   */
  public boolean isAuthorized() {
    SessionInfo sessionInfo = getSessionInfo();
    if (sessionInfo == null) {
      return false;
    }
    return true;
  }

  /**
   * Sets expiration date of the session cookie.
   */
  public void setSessionCookieMaxAge() {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return;
    }
    int sessionTimeoutSec = getConfigValueAsInteger("session_timeout_sec", 86400);
    setSessionCookieMaxAge(sessionId, sessionTimeoutSec);
  }

  /**
   * Sets expiration date of the session cookie.
   * 
   * @param sessionId
   *          the target session id
   * @param sessionTimeoutSec
   *          the session timeout in seconds
   */
  public void setSessionCookieMaxAge(String sessionId, int sessionTimeoutSec) {
    SessionManager sessionManager = getSessionManager();
    String cookieName = sessionManager.getSessionCoolieName();
    Cookie cookie = new Cookie(cookieName, sessionId);
    cookie.setMaxAge(sessionTimeoutSec);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }

  /**
   * Returns the application manager object.
   *
   * @return AppManager
   */
  public AppManager getAppManager() {
    AppManager appManager = AppManager.getInstance();
    return appManager;
  }

  /**
   * Returns the session manager object. *
   *
   * @return SessionManager
   */
  public SessionManager getSessionManager() {
    AppManager appManager = getAppManager();
    SessionManager sessionManager = appManager.getSessionManager();
    return sessionManager;
  }

  /**
   * Returns the user manager object.
   *
   * @return UserManager
   */
  public UserManager getUserManager() {
    AppManager appManager = getAppManager();
    UserManager userManager = appManager.getUserManager();
    return userManager;
  }

  /**
   * Returns the gtoup manager object.
   *
   * @return GroupManager
   */
  public GroupManager getGroupManager() {
    AppManager appManager = getAppManager();
    GroupManager groupManager = appManager.getGroupManager();
    return groupManager;
  }

  /**
   * Returns the async task manager object.
   *
   * @return AsyncTaskManager
   */
  public AsyncTaskManager getAsyncManager() {
    AppManager appManager = getAppManager();
    AsyncTaskManager asyncTaskManager = appManager.getAsyncTaskManager();
    return asyncTaskManager;
  }

  /**
   * Returns whether the screen is enabled.<br>
   * Returns true unless the config setting explicitly specifies false.
   *
   * @param screenId
   *          the screen id
   * @return true if the screen is enabled; false otherwise.
   */
  public boolean isScreenEnabled(String screenId) {
    String key = "screen_" + screenId;
    if (!hasConfigValue(key) || getConfigValueAsBoolean(key)) {
      return true;
    }
    return false;
  }

}
