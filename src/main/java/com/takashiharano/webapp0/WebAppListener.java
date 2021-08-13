package com.takashiharano.webapp0;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.takashiharano.webapp0.util.Log;

@WebListener()
public class WebAppListener implements ServletContextListener, HttpSessionListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    AppManager.onStart();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    AppManager.onStop();
  }

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    Log.i("sessionCreated: " + session.getId());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    Log.i("sessionDestroyed: " + session.getId());
  }

}
