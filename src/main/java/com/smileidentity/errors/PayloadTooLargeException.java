package com.smileidentity.errors;

/** HTTP 413: an uploaded image or the overall payload is too large. */
public class PayloadTooLargeException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public PayloadTooLargeException(String message) {
    super(message);
  }

  public PayloadTooLargeException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }
}
