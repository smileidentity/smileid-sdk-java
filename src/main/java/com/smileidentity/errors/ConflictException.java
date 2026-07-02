package com.smileidentity.errors;

/**
 * HTTP 409: a business-state conflict, for example replaying a callback for a verification that is
 * still processing. Never auto-retried (spec §2.6).
 */
public class ConflictException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public ConflictException(String message) {
    super(message);
  }

  public ConflictException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }
}
