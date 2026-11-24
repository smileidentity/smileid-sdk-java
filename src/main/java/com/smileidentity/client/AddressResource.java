package com.smileidentity.client;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AddressAcceptedResponse;
import com.smileidentity.generated.models.AddressVerificationParams;
import com.smileidentity.generated.operations.AddressOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.address()} — POST /v3/address. */
public final class AddressResource {

  private final Transport transport;

  AddressResource(Transport transport) {
    this.transport = transport;
  }

  /**
   * POST /v3/address — queues an address verification; the result is delivered to the callback URL.
   * Never retried. The token is bound to the {@code address_verification} product, and to {@code
   * userId} when the params set one.
   */
  public AddressAcceptedResponse verify(AddressVerificationParams params) {
    return verify(params, RequestOptions.none());
  }

  public AddressAcceptedResponse verify(AddressVerificationParams params, RequestOptions options) {
    requireText(params.getAddress(), "address");
    requireText(params.getCountry(), "country");
    if (isBlank(params.getIdNumber()) && isBlank(params.getUtilityNumber())) {
      throw new ValidationException("either id_number or utility_number is required");
    }
    Validators.requireEmailOrPhone(params.getUserDetails());
    if (params.getConsent() == null) {
      throw new ValidationException("consent is required");
    }
    return AddressOperations.addressVerification(transport, params, options);
  }

  private static void requireText(String value, String name) {
    if (isBlank(value)) {
      throw new ValidationException(name + " is required");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
