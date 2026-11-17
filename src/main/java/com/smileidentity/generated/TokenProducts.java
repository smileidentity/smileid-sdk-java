package com.smileidentity.generated;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code product} token claim each operation requires, from the spec's {@code
 * x-smile-token-product} extension (v3.1188.0). Paths absent here need a token with no {@code
 * product} claim.
 */
public final class TokenProducts {

  private static final Map<String, String> BY_PATH = byPath();

  private TokenProducts() {}

  /** The product for a request path, or {@code null} when the operation has none. */
  public static String forPath(String path) {
    return BY_PATH.get(path);
  }

  private static Map<String, String> byPath() {
    Map<String, String> m = new HashMap<>();
    m.put("/v3/address", "address_verification");
    m.put("/v3/aml/monitoring", "aml");
    m.put("/v3/authentication", "smart_selfie_authentication");
    m.put("/v3/registration", "smart_selfie_registration");
    m.put("/v3/biometric_kyc", "biometric_kyc");
    m.put("/v3/document_verification", "document_verification");
    m.put("/v3/enhanced_document_verification", "enhanced_document_verification");
    m.put("/v3/enhanced_kyc", "enhanced_kyc");
    m.put("/v3/enhanced_kyc/sync", "enhanced_kyc");
    m.put("/v3/aml", "one_time_aml");
    m.put("/v3/aml/sync", "one_time_aml");
    m.put("/v3/phone", "phone_number_verification");
    m.put("/v3/residency_document_verification", "residency_document_verification");
    m.put("/v3/compare", "smart_selfie_compare");
    return Collections.unmodifiableMap(m);
  }
}
