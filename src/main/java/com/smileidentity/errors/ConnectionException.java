package com.smileidentity.errors;

/** A network failure or timeout with no HTTP response. {@code getStatusCode()} is null. */
public class ConnectionException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public ConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
