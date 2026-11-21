package com.smileidentity.errors;

/** HTTP 5xx (and any status without a more specific mapping): a server-side failure. */
public class ApiException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public ApiException(String message) {
    super(message);
  }

  public ApiException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public ApiException(
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
