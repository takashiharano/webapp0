package com.takashiharano.webapp0.action;

import java.io.IOException;

import javax.servlet.ServletException;

import com.takashiharano.webapp0.AppManager;
import com.takashiharano.webapp0.NotAuthorizedException;
import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.session.Authenticator;

/**
 * Show screen.
 */
public class ShowScreenAction extends Action {

  @Override
  protected void init(ProcessContext context) {
    setAuthRequired(false);
    context.setResponseType("html");
  }

  public void process(ProcessContext context) throws Exception {
    String screen = context.getRequestParameter("screen");
    if (screen == null) {
      // context.sendErrorScreen("No Such Screen: " + screen);
      // return;
      screen = "main";
    }
    if (AppManager.getInstance().isReady()) {
      if (isAuthRequired(screen)) {
        boolean authorized = Authenticator.checkAuthorization(context);
        if (!authorized) {
          throw new NotAuthorizedException();
        }
      }

      String screenFile = screen + ".jsp";
      context.forward(screenFile);
    } else {
      showErrorScreen(context);
    }
  }

  private void showErrorScreen(ProcessContext context) throws ServletException, IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("ERROR:\n");
    sb.append("The system is not working properly.\n");
    sb.append("Cause: ");
    sb.append(AppManager.getInstance().getErrorInfo());
    sb.append("\n\n");
    sb.append("Further investigation is required.");
    context.sendErrorScreen(sb.toString());
  }

  private boolean isAuthRequired(String screenName) {
    String[] NO_AUTH_REQUIRED = { "main1" };
    for (int i = 0; i < NO_AUTH_REQUIRED.length; i++) {
      if (NO_AUTH_REQUIRED[i].equals(screenName)) {
        return false;
      }
    }
    return true;
  }

}
