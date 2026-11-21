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
   * completed throws {@code ApiException} (408, 424 or 500). The job may still have run, and
   * resubmitting runs and bills a second lookup, so don't resubmit automatically. The client's
   * default callback URL is never sent, because this endpoint ignores it; a callback URL set on the
   * params is sent as given.
   */
  public BasicKycResult verifySync(BasicKycParams params) {
    return verifySync(params, RequestOptions.none());
  }

  public BasicKycResult verifySync(BasicKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return BasicKycOperations.basicKycSync(transport, params, options);
  }
}
