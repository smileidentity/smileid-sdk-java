package com.smileidentity.errors;

/** HTTP 401: invalid credentials, or a token refresh that failed twice. */
public class AuthenticationException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }
}
