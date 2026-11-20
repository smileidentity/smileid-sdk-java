package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.BasicKycParams;
import com.smileidentity.generated.models.BasicKycResult;
import com.smileidentity.generated.operations.BasicKycOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.basicKyc()} — POST /v3/basic_kyc and POST /v3/basic_kyc/sync. */
public final class BasicKycResource {

  private final Transport transport;

  BasicKycResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/basic_kyc — queues the check; the result is delivered to the callback URL. */
  public AcceptedResponse verify(BasicKycParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(BasicKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return BasicKycOperations.basicKyc(transport, params, options);
  }

  /**
   * POST /v3/basic_kyc/sync — runs the check and returns the result. A check that could not be
   * completed throws {@code ApiException} (408, 424 or 500); the job is recorded, so read its
   * outcome with the status lookup instead of submitting again, which would bill a second lookup.
   */
  public BasicKycResult verifySync(BasicKycParams params) {
    return verifySync(params, RequestOptions.none());
  }

  public BasicKycResult verifySync(BasicKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return BasicKycOperations.basicKycSync(transport, params, options);
  }
}
