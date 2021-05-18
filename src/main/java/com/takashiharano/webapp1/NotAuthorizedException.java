package com.takashiharano.webapp1;

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
