package com.smileidentity.errors;

/** HTTP 429: rate limit exceeded. */
public class RateLimitException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public RateLimitException(String message) {
    super(message);
  }

  public RateLimitException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public RateLimitException(
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
