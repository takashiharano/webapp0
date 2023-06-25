/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0.action;

import com.takashiharano.webapp0.ProcessContext;
import com.takashiharano.webapp0.logic.ShowScreenLogic;

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
      screen = "dashboard";
    }
    ShowScreenLogic.process(context, screen);
  }

}
