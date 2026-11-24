package com.smileidentity.client;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.PhoneVerificationParams;
import com.smileidentity.generated.operations.PhoneOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.phone()} — POST /v3/phone. */
public final class PhoneResource {

  private final Transport transport;

  PhoneResource(Transport transport) {
    this.transport = transport;
  }

  /**
   * POST /v3/phone — queues a phone number verification; the result is delivered to the callback
   * URL. Never retried. The token is bound to the {@code phone_number_verification} product, and to
   * {@code userId} when the params set one.
   */
  public AcceptedResponse verify(PhoneVerificationParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(PhoneVerificationParams params, RequestOptions options) {
    requireText(params.getCountry(), "country");
    requireText(params.getPhoneNumber(), "phone_number");
    Validators.requireEmailOrPhone(params.getUserDetails());
    if (params.getConsent() == null) {
      throw new ValidationException("consent is required");
    }
    return PhoneOperations.phoneVerification(transport, params, options);
  }

  private static void requireText(String value, String name) {
    if (value == null || value.trim().isEmpty()) {
      throw new ValidationException(name + " is required");
    }
  }
}
