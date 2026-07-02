package com.smileidentity.errors;

/** HTTP 402: insufficient wallet balance. */
public class PaymentRequiredException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public PaymentRequiredException(String message) {
    super(message);
  }

  public PaymentRequiredException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }
}
