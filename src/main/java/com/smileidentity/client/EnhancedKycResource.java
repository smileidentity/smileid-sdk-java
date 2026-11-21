package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.EnhancedKycResult;
import com.smileidentity.generated.operations.KycOperations;
import com.smileidentity.helpers.Validators;

/**
 * {@code client.enhancedKyc()} — POST /v3/enhanced_kyc (spec §4, §6.1) and POST
 * /v3/enhanced_kyc/sync.
 */
public final class EnhancedKycResource {

  private final Transport transport;

  EnhancedKycResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/enhanced_kyc — queues the check; the result is delivered to the callback URL. */
  public AcceptedResponse verify(EnhancedKycParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(EnhancedKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return KycOperations.enhancedKyc(transport, params, options);
  }

  /**
   * POST /v3/enhanced_kyc/sync — runs the check and returns the result (the same payload the
   * callback would carry), with the job id from the {@code SmileID-Job-ID} header. A check that
   * could not be completed throws {@code ApiException} (408, 424 or 500). The job may still have
   * run, and resubmitting runs and bills a second lookup, so don't resubmit automatically: read the
   * outcome with {@code verifications().retrieve(e.getJobId())}. A 500 with no job id means the job
   * wasn't created. The client's default callback URL is never sent, because this endpoint ignores
   * it; a callback URL set on the params is sent as given.
   */
  public EnhancedKycResult verifySync(EnhancedKycParams params) {
    return verifySync(params, RequestOptions.none());
  }

  public EnhancedKycResult verifySync(EnhancedKycParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return KycOperations.enhancedKycSync(transport, params, options);
  }
}
