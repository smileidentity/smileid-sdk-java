package com.smileidentity.errors;

/** HTTP 403: the partner is not authorised for this action or product. */
public class PermissionException extends SmileIDException {

  private static final long serialVersionUID = 1L;

  public PermissionException(String message) {
    super(message);
  }

  public PermissionException(
      String message,
      Integer statusCode,
      String status,
      String code,
      String requestId,
      String rawBody) {
    super(message, statusCode, status, code, requestId, rawBody);
  }
}
