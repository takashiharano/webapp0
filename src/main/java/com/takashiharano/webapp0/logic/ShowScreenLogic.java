package com.takashiharano.webapp0.logic;

import java.io.IOException;

import javax.servlet.ServletException;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.NotAuthorizedException;
import com.takashiharano.webapp0.ProcessContext;

public class ShowScreenLogic {

  public static void process(ProcessContext context, String screen) throws Exception {
    AppManager appManager = context.getAppManager();
    if (appManager.isReady()) {
      if (isAuthRequired(screen)) {
        if (!context.isAuthorized()) {
          String message = "Access denied. (screen=" + screen + ")";
          String requestedUri = context.getRequestedUri();
          requestedUri = requestedUri.substring(1); // remove leading "/"
          throw new NotAuthorizedException(message, requestedUri);
        }
      }

      context.setInfo("screenId", screen);
      String screenFile = screen + ".jsp";
      context.forward(screenFile);
    } else {
      String errorInfo = appManager.getErrorInfo();
      showErrorScreen(context, errorInfo);
    }
  }

  private static void showErrorScreen(ProcessContext context, String cause) throws ServletException, IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("ERROR: The system is not working properly.\n\n");
    sb.append("Cause: ");
    sb.append(cause);
    String errorInfo = sb.toString();
    context.sendErrorScreen(errorInfo);
  }

  private static boolean isAuthRequired(String screenName) {
    String[] NO_AUTH_REQUIRED = { "main1" };
    for (int i = 0; i < NO_AUTH_REQUIRED.length; i++) {
      if (NO_AUTH_REQUIRED[i].equals(screenName)) {
        return false;
      }
    }
    return true;
  }

}
