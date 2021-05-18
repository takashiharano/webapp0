package com.takashiharano.webapp1;

import java.io.IOException;

import javax.servlet.ServletException;

import com.takashiharano.util.Log;

public class DefaultErrorHandler {
  /**
   * Handle errors.
   *
   * @param context
   *          process context
   * @param t
   *          throwable object
   * @throws ServletException
   * @throws IOException
   */
  public static void handle(ProcessContext context, Throwable t) throws ServletException, IOException {
    if (t instanceof NotAuthorizedException) {
      handleNotAuthorized(context, (NotAuthorizedException) t);
    } else {
      handleDefaultException(context, t);
    }
  }

  /**
   * general error handler
   *
   * @param context
   *          process context
   * @param t
   *          throwable object
   * @throws ServletException
   * @throws IOException
   */
  private static void handleDefaultException(ProcessContext context, Throwable t) throws ServletException, IOException {
    Log.e(t);
    String actionName = context.getActionName();
    String message = actionName + "Action: " + t.toString();
    if ("html".equals(context.getResponseType())) {
      context.sendErrorScreen(message);
    } else if ("text".equals(context.getResponseType())) {
      context.sendTextResponse("ERROR: " + message);
    } else {
      context.sendJsonResponse("ERROR", message);
    }
  }

  /**
   * Error handler for authorization error.
   *
   * @param context
   *          process context
   * @param e
   *          exception object
   * @throws ServletException
   * @throws IOException
   */
  private static void handleNotAuthorized(ProcessContext context, NotAuthorizedException e)
      throws ServletException, IOException {
    if ("json".equals(context.getResponseType())) {
      context.sendJsonResponse("FORBIDDEN", null);
    } else {
      String requestedUrl = e.getUrl();
      context.setInfo("requestedUrl", requestedUrl);
      context.forward("login.jsp");
    }
  }
}
