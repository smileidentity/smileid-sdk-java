package com.smileidentity.errors;

/**
 * Base class for every error raised by the Smile ID SDK (spec §7).
 *
 * <p>Every error exposes the HTTP status code (null for connection or local errors), the HTTP
 * status text from the body when present, the human-readable message, the service error code when
 * present (only on the {@code {error, code}} wire shape), the request id when one exists, and the
 * unparsed response body.
 */
public class SmileIDException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Integer statusCode;
  private final String status;
  private final String code;
  private final String requestId;
  private final String rawBody;

  public SmileIDException(String message) {
    this(message, null, null, null, null, null, null);
  }

  public SmileIDException(String message, Throwable cause) {
    this(message, null, null, null, null, null, cause);
  }

  public SmileIDException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    this(message, statusCode, status, code, requestId, rawBody, null);
  }

  public SmileIDException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody,
      Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.status = status;
    this.code = code;
    this.requestId = requestId;
    this.rawBody = rawBody;
  }

  /** HTTP status code, or null for connection errors and local validation errors. */
  public Integer getStatusCode() {
    return statusCode;
  }

  /** HTTP status text from the response body (e.g. "Bad Request"), or null. */
  public String getStatus() {
    return status;
  }

  /** Service error code (only present on the services {error, code} shape), or null. */
  public String getCode() {
    return code;
  }

  /** Request id from a response header when one exists, or null. */
  public String getRequestId() {
    return requestId;
  }

  /** The unparsed response body, or null. */
  public String getRawBody() {
    return rawBody;
  }
}
