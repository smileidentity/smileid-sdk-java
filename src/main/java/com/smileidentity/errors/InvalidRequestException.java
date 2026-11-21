package com.smileidentity.errors;

/** HTTP 400 or 415: the request was malformed or used an unsupported media type. */
public class InvalidRequestException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }

  /** As above, also carrying the job id from the SmileID-Job-ID response header (may be null). */
  public InvalidRequestException(
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
