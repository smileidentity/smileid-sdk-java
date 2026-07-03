package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.operations.BiometricOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.biometric()} — enrollment, authentication and compare (spec §4, §6.5–§6.7). */
public final class BiometricResource {

  private final Transport transport;

  BiometricResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/registration. */
  public AcceptedResponse enroll(EnrollParams params) {
    return enroll(params, RequestOptions.none());
  }

  public AcceptedResponse enroll(EnrollParams params, RequestOptions options) {
    Validators.validateEnroll(params);
    return BiometricOperations.registration(transport, params, options);
  }

  /** POST /v3/authentication — user_id required, images required unless useEnrolledImage. */
  public AcceptedResponse authenticate(AuthenticationParams params) {
    return authenticate(params, RequestOptions.none());
  }

  public AcceptedResponse authenticate(AuthenticationParams params, RequestOptions options) {
    Validators.validateAuthenticationImages(params);
    return BiometricOperations.authentication(transport, params, options);
  }

  /** POST /v3/compare. */
  public AcceptedResponse compare(CompareParams params) {
    return compare(params, RequestOptions.none());
  }

  public AcceptedResponse compare(CompareParams params, RequestOptions options) {
    Validators.validateCompare(params);
    return BiometricOperations.compare(transport, params, options);
  }
}
