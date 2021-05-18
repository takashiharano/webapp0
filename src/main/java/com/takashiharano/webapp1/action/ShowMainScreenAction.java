package com.takashiharano.webapp1.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.takashiharano.webapp1.ProcessContext;
import com.takashiharano.webapp1.ServletUtil;

public class ShowMainScreenAction extends Action {

  @Override
  public void process(ProcessContext context) throws Exception {
    long timestamp = System.currentTimeMillis();
    Date date = new Date(timestamp);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    String datetime = sdf.format(date);

    ServletContext servletContext = context.getServletContext();
    String appVersion = ServletUtil.getManifestEntry(servletContext, "App-Version");

    StringBuilder sb = new StringBuilder();
    sb.append(datetime + " (" + timestamp + ")\n");
    sb.append("App-Version: " + appVersion);

    HttpServletRequest request = context.getRequest();
    request.setAttribute("info", sb.toString());
    context.forward("main.jsp");
  }

}
