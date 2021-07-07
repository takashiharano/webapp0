package com.takashiharano.webapp1;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.libutil.Base64Util;
import com.libutil.FileUtil;
import com.libutil.JsonBuilder;
import com.takashiharano.webapp1.util.Log;

public class ProcessContext {
  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletContext servletContext;
  private String responseType;
  private String actionName;
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
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public String getRequestParameter(String key) {
    return request.getParameter(key);
  }

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

  public String getBase64DecodedRequestParameter(String key) {
    String value = getRequestParameter(key);
    if (value == null) {
      return null;
    }
    return Base64Util.decode(value);
  }

  public void forward(String path) throws IOException, ServletException {
    request.getRequestDispatcher(path).forward(request, response);
  }

  public String getActionName() {
    return actionName;
  }

  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  public void setResponseCode(int status) {
    response.setStatus(status);
  }

  public void setResponseHeader(String name, String value) {
    response.setHeader(name, value);
  }

  public void sendTextResponse(String text) throws IOException {
    sendResponse("text/plain", text);
  }

  public void sendJsonResponse(String json) {
    sendResponse("application/json", json);
  }

  public void sendJsonResponse(String status, String body) {

  }

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

  public void sendResponse(String contentType, String text) {
    response.setContentType(contentType + ";charset=utf-8");
    try (PrintWriter writer = response.getWriter()) {
      writer.write(text);
    } catch (IOException e) {
      Log.e(e);
    }
  }

  public void sendJson(String json) throws IOException {
    sendJsonResponse(json);
  }

  public void sendJson(JsonBuilder jb) throws IOException {
    sendJsonResponse(jb.toString());
  }

  public void sendJson(String status, String body) throws IOException {
    JsonBuilder jb = new JsonBuilder();
    jb.append("status", status);
    jb.appendObject("body", body);
    sendJsonResponse(jb.toString());
  }

  public void sendByteStreamResponse(byte[] b, String fileName) throws IOException {
    setStreamResponseHeader(fileName);
    sendByteArray(b);
  }

  public void sendTextStreamResponse(String text, String fileName) throws IOException {
    setStreamResponseHeader(fileName);
    byte[] b = text.getBytes("UTF-8");
    sendByteArray(b);
  }

  public void sendFileResponse(String path) throws IOException {
    sendFileResponse(path, null);
  }

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
    try (ServletOutputStream os = response.getOutputStream();
        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);) {

      byte[] b = new byte[1 * 1024 * 1024];
      int readSize = 0;

      while (-1 != (readSize = dis.read(b))) {
        os.write(b, 0, readSize);
      }
    } catch (IOException ioe) {
      throw ioe;
    }
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  public HashMap<String, Object> getInfo() {
    return info;
  }

  public void setInfo(String key, Object value) {
    info.put(key, value);
  }

  public void sendErrorScreen(String errorInfo) throws ServletException, IOException {
    setInfo("errorInfo", errorInfo);
    String path = "error.jsp";
    Log.i("Screen => " + path);
    forward(path);
  }

}
