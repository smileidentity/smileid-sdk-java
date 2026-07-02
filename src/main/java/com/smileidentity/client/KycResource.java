package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.EnhancedKycParams;

/**
 * {@code client.kyc()} — convenience namespace matching the spec §8 sample ({@code
 * smile.kyc().enhanced(...)}). Delegates to {@link EnhancedKycResource}.
 */
public final class KycResource {

  private final EnhancedKycResource enhancedKyc;

  KycResource(EnhancedKycResource enhancedKyc) {
    this.enhancedKyc = enhancedKyc;
  }

  public AcceptedResponse enhanced(EnhancedKycParams params) {
    return enhancedKyc.verify(params);
  }

  public AcceptedResponse enhanced(EnhancedKycParams params, RequestOptions options) {
    return enhancedKyc.verify(params, options);
  }
}
