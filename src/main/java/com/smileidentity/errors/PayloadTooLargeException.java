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

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public PayloadTooLargeException(
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
