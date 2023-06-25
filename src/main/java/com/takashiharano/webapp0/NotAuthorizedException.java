/*
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
package com.takashiharano.webapp0;

public class NotAuthorizedException extends Exception {

  private static final long serialVersionUID = -1019522855691200724L;
  private String url;

  public NotAuthorizedException() {
    super();
  }

  public NotAuthorizedException(String message) {
    super(message);
  }

  public NotAuthorizedException(String message, String url) {
    super(message);
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

}
