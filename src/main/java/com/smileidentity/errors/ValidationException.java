package com.smileidentity.errors;

/**
 * Raised locally, before any request is sent, when client-side validation fails (for example a
 * missing email or phone number in user details, or invalid fraud-report field combinations).
 */
public class ValidationException extends InvalidRequestException {

  private static final long serialVersionUID = 1L;

  public ValidationException(String message) {
    super(message);
  }
}
