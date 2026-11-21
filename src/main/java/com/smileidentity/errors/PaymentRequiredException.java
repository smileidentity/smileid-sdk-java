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

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public PaymentRequiredException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String jobId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, jobId, rawBody, null);
  }
}
