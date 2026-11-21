package com.smileidentity.errors;

/**
 * HTTP 404. Note that {@code verifications().retrieve(...)} never raises this: a 404 from the
 * status endpoint returns a JobStatus with status "not_found" instead (spec §6.8).
 */
public class NotFoundException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public NotFoundException(
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
