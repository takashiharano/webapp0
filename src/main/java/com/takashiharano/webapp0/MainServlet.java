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

@WebServlet(name = "MainServlet", urlPatterns = ("/main"))
@MultipartConfig(fileSizeThreshold = 0, location = "", maxFileSize = -1L, maxRequestSize = -1L)
public class MainServlet extends HttpServlet {

  private static final long serialVersionUID = -5960501271818225697L;

  private static final String DEFAULT_ACTION_NAME = "ShowMainScreen";

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // HttpSession session = request.getSession(true);

    ServletContext servletContext = getServletContext();
    ProcessContext context = new ProcessContext(request, response, servletContext);

    try {
      _service(context);
    } catch (Throwable t) {
      DefaultErrorHandler.handle(context, t);
    }

    // session.invalidate();
  }

  protected void _service(ProcessContext context) throws Throwable {
    String method = context.getRequest().getMethod();
    if (checkMethod(method) == false) {
      context.setResponseCode(405); // Method Not Allowed
      context.sendTextResponse("Method " + method + " is not allowed.");
      return;
    }

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

    Action.exec(context, actionName);
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
