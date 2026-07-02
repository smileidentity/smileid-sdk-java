package com.smileidentity.errors;

/**
 * Raised by {@code verifications().waitUntilComplete(...)} when the job does not reach a terminal
 * state before the polling deadline. SDK-local: {@code getStatusCode()} is null.
 */
public class TimeoutException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public TimeoutException(String message) {
    super(message);
  }
}
