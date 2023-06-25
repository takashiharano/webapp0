/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
package com.takashiharano.webapp0.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.libutil.JsonBuilder;
import com.takashiharano.webapp0.ProcessContext;

public class JsonAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    HttpServletRequest request = context.getRequest();

    String datetime = getDateTimeString();
    String method = request.getMethod();
    String params = context.dumpParameters();

    JsonBuilder jb = new JsonBuilder();
    jb.append("datetime", datetime);
    jb.append("method", method);
    jb.append("params", params);

    String body = jb.toString();
    context.sendJsonResponse("OK", body);
  }

  private String getDateTimeString() {
    String DATE_FORMAT_ISO8601EXTZ = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_ISO8601EXTZ);
    String strDate = sdf.format(date);
    return strDate;
  }

}
