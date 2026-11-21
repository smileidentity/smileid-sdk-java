package com.smileidentity.generated.operations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code x-smile-token-product} of each operation in the spec, keyed by spec path template. An
 * operation absent here requires a token with no {@code product} claim.
 */
public final class OperationProducts {

  private static final Map<String, String> PRODUCT_BY_PATH = productByPath();

  private OperationProducts() {}

  /** Returns the product claim for a spec path template, or null when the token has none. */
  public static String forPath(String pathTemplate) {
    return PRODUCT_BY_PATH.get(pathTemplate);
  }

  private static Map<String, String> productByPath() {
    Map<String, String> m = new HashMap<>();
    m.put("/v3/address", "address_verification");
    m.put("/v3/aml", "one_time_aml");
    m.put("/v3/aml/monitoring", "aml");
    m.put("/v3/aml/monitoring/{job_id}/news/{reference}", "aml");
    m.put("/v3/aml/sync", "one_time_aml");
    m.put("/v3/authentication", "smart_selfie_authentication");
    m.put("/v3/basic_kyc", "basic_kyc");
    m.put("/v3/basic_kyc/sync", "basic_kyc");
    m.put("/v3/biometric_kyc", "biometric_kyc");
    m.put("/v3/compare", "smart_selfie_compare");
    m.put("/v3/document_verification", "document_verification");
    m.put("/v3/enhanced_document_verification", "enhanced_document_verification");
    m.put("/v3/enhanced_kyc", "enhanced_kyc");
    m.put("/v3/enhanced_kyc/sync", "enhanced_kyc");
    m.put("/v3/phone", "phone_number_verification");
    m.put("/v3/registration", "smart_selfie_registration");
    m.put("/v3/residency_document_verification", "residency_document_verification");
    return Collections.unmodifiableMap(m);
  }
}
