package com.takashiharano.webapp0;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ServletUtil {

  /**
   * Returns the Cookie value for the given name.
   *
   * @param context
   *          process context
   * @param name
   *          field name
   * @return Cookie value. null if not found.
   */
  public static String getCookie(ProcessContext context, String name) {
    HttpServletRequest request = context.getRequest();
    String value = null;
    Cookie[] cookies = request.getCookies();
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
   * Dump the request parameters.
   *
   * @param request
   *          HTTP request
   * @return all request parameters
   */
  public static String dumpParameters(HttpServletRequest request) {
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

      if ((params.size() > 1) || (!"".contentEquals(values[0]))) {
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
   * @param servletContext
   *          Servlet context
   * @param name
   *          field name
   * @return the value
   * @throws IOException
   *           If an IO error occurs
   */
  public static String getManifestEntry(ServletContext servletContext, String name) throws IOException {
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

}
