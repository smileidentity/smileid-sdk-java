package com.smileidentity.errors;

/**
 * A success-path (2xx) response body was not a JSON object (fleet standard, 2026-07-03). Carries
 * the HTTP status code, the request id when present, and the raw body for diagnosis. The {@code
 * verifications().retrieve} 404-to-not_found path is unaffected.
 */
public class UnexpectedResponseException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public UnexpectedResponseException(
      String message, Integer statusCode, String requestId, String rawBody) {
    super(message, statusCode, null, null, requestId, rawBody);
  }
}
