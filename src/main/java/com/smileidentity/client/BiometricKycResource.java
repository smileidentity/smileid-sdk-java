package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.operations.BiometricOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.biometricKyc()} — POST /v3/biometric_kyc (spec §4, §6.4). */
public final class BiometricKycResource {

  private final Transport transport;

  BiometricKycResource(Transport transport) {
    this.transport = transport;
  }

  public AcceptedResponse verify(BiometricKycParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(BiometricKycParams params, RequestOptions options) {
    Validators.validateBiometricKyc(params);
    return BiometricOperations.biometricKyc(transport, params, options);
  }
}
