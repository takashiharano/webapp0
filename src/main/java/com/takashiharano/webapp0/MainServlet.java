package com.takashiharano.webapp0;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.takashiharano.webapp0.action.Action;
import com.takashiharano.webapp0.logic.ShowScreenLogic;
import com.takashiharano.webapp0.util.Log;

@WebServlet(name = "MainServlet", urlPatterns = ("/main"))
@MultipartConfig(fileSizeThreshold = 0, location = "", maxFileSize = -1L, maxRequestSize = -1L)
public class MainServlet extends HttpServlet {

  private static final long serialVersionUID = -5960501271818225697L;

  private static final String DEFAULT_ACTION_NAME = "ShowScreen";

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletContext servletContext = getServletContext();
    ProcessContext context = new ProcessContext(request, response, servletContext);

    context.onAccess();

    try {
      _service(context);
    } catch (Throwable t) {
      DefaultErrorHandler.handle(context, t);
    }

    context.onAccessEnd();
  }

  protected void _service(ProcessContext context) throws Throwable {
    String method = context.getRequest().getMethod();
    if (checkMethod(method) == false) {
      context.setResponseCode(405); // Method Not Allowed
      context.sendTextResponse("Method " + method + " is not allowed.");
      return;
    }

    String screen = context.getRequestParameter("screen");
    if (screen != null) {
      ShowScreenLogic.process(context, screen);
      return;
    }

    String actionName = getActionName(context);
    Action action = Action.getActionInstance(context, actionName);
    if (action == null) {
      Log.e("ACTION_NOT_FOUND: " + actionName);
      context.sendJson("ACTION_NOT_FOUND", actionName);
      return;
    }

    if (action.isAuthRequired()) {
      if (!context.isAuthorized()) {
        String message = "Access denied. (action=" + actionName + ")";
        String requestedUri = context.getRequestedUri();
        requestedUri = requestedUri.substring(1); // remove leading "/"
        throw new NotAuthorizedException(message, requestedUri);
      }
    }

    action.process(context);
  }

  private String getActionName(ProcessContext context) {
    String actionName = context.getActionName();
    if (actionName == null) {
      if (context.getRequestParameter("version") != null) {
        // ?version
        actionName = "version";
      } else if (context.getRequestParameter("screen") != null) {
        // ?screen=xxx
        actionName = "ShowScreen";
      } else {
        actionName = DEFAULT_ACTION_NAME;
      }
    }
    return actionName;
  }

  private boolean checkMethod(String method) {
    String[] ALLOWED_METHOD = { "GET", "POST", "HEAD" };
    for (int i = 0; i < ALLOWED_METHOD.length; i++) {
      if (ALLOWED_METHOD[i].equals(method)) {
        return true;
      }
    }
    return false;
  }

}
