package com.takashiharano.webapp1.action;

import java.io.IOException;

import javax.servlet.ServletException;

import com.takashiharano.webapp1.AppManager;
import com.takashiharano.webapp1.ProcessContext;

/**
 * Show screen.
 */
public class ShowScreenAction extends Action {

  protected void init(ProcessContext context) {
    context.setResponseType("html");
  }

  public void process(ProcessContext context) throws Exception {
    String screen = context.getRequestParameter("screen");
    if (screen == null) {
      context.sendErrorScreen("No Such Screen: " + screen);
      return;
    }
    if (AppManager.isReady()) {
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
    sb.append(AppManager.getErrorInfo());
    sb.append("\n\n");
    sb.append("Further investigation is required.");
    context.sendErrorScreen(sb.toString());
  }

}
