package com.takashiharano.webapp1.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.takashiharano.util.JsonBuilder;
import com.takashiharano.webapp1.ProcessContext;
import com.takashiharano.webapp1.ServletUtil;

public class JsonAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    HttpServletRequest request = context.getRequest();

    String datetime = getDateTimeString();
    String method = request.getMethod();
    String params = ServletUtil.dumpParameters(request);

    JsonBuilder jb = new JsonBuilder();
    jb.append("datetime", datetime);
    jb.append("method", method);
    jb.append("params", params);

    String body = jb.toString();
    context.sendJson("OK", body);
  }

  private String getDateTimeString() {
    String DATE_FORMAT_ISO8601EXTZ = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_ISO8601EXTZ);
    String strDate = sdf.format(date);
    return strDate;
  }

}
