package com.oneops.gslb;

public class ExecutionException extends Exception {

  public ExecutionException(String message) {
    super(message);
  }

  public ExecutionException(String message, Throwable t) {
    super(message, t);
  }

}
