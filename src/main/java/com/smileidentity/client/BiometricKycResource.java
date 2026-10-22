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
    return send(params, false, options);
  }

  /**
   * Verifies against the user's enrolled selfie: the grant is bound into this job's token, which
   * names {@code params.getUserId()}, so the selfie and liveness images may be omitted.
   */
  public AcceptedResponse verifyWithEnrolledImage(BiometricKycParams params) {
    return verifyWithEnrolledImage(params, RequestOptions.none());
  }

  public AcceptedResponse verifyWithEnrolledImage(
      BiometricKycParams params, RequestOptions options) {
    return send(params, true, options);
  }

  private AcceptedResponse send(
      BiometricKycParams params, boolean useEnrolledImage, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    Validators.validateBiometricKycInputs(params, useEnrolledImage);
    ApiRequest request = BiometricOperations.biometricKycRequest(transport, params, options);
    return transport.execute(request, AcceptedResponse.class, useEnrolledImage);
  }
}
