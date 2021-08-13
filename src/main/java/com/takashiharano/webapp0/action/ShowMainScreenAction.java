package com.takashiharano.webapp0.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.ServletUtil;

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

    context.setInfo("info", sb.toString());
    context.forward("main.jsp");
  }

}
