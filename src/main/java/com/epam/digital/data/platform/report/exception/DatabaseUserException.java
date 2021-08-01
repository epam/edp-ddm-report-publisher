package com.epam.digital.data.platform.report.exception;

public class DatabaseUserException extends RuntimeException {
  public DatabaseUserException(String msg, Throwable cause) {
    super(msg);
  }
}
