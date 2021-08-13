package com.takashiharano.webapp0;

import java.io.IOException;

import javax.servlet.ServletException;

import com.takashiharano.webapp0.util.Log;

public class DefaultErrorHandler {
  /**
   * Handle errors.
   *
   * @param context
   *          process context
   * @param t
   *          throwable object
   * @throws ServletException
   *           If the Servlet encounters difficulty
   * @throws IOException
   *           If an I/O error occurs
   */
  public static void handle(ProcessContext context, Throwable t) throws ServletException, IOException {
    if (t instanceof NotAuthorizedException) {
      handleNotAuthorized(context, (NotAuthorizedException) t);
    } else {
      handleDefaultException(context, t);
    }
  }

  /**
   * General error handler.
   *
   * @param context
   *          process context
   * @param t
   *          throwable object
   * @throws ServletException
   *           If the Servlet encounters difficulty
   * @throws IOException
   *           If an I/O error occurs
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
   *           If the Servlet encounters difficulty
   * @throws IOException
   *           If an I/O error occurs
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
