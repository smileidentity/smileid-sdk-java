package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.operations.KycOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.enhancedKyc()} — POST /v3/enhanced_kyc (spec §4, §6.1). */
public final class EnhancedKycResource {

  private final Transport transport;

  EnhancedKycResource(Transport transport) {
    this.transport = transport;
  }

  public AcceptedResponse verify(EnhancedKycParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(EnhancedKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return KycOperations.enhancedKyc(transport, params, options);
  }
}
