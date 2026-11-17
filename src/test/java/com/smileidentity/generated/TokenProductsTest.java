package com.smileidentity.generated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TokenProductsTest {

  @ParameterizedTest
  @CsvSource({
    "/v3/address,address_verification",
    "/v3/aml/monitoring,aml",
    "/v3/authentication,smart_selfie_authentication",
    "/v3/registration,smart_selfie_registration",
    "/v3/biometric_kyc,biometric_kyc",
    "/v3/document_verification,document_verification",
    "/v3/enhanced_document_verification,enhanced_document_verification",
    "/v3/enhanced_kyc,enhanced_kyc",
    "/v3/enhanced_kyc/sync,enhanced_kyc",
    "/v3/aml,one_time_aml",
    "/v3/aml/sync,one_time_aml",
    "/v3/phone,phone_number_verification",
    "/v3/residency_document_verification,residency_document_verification",
    "/v3/compare,smart_selfie_compare"
  })
  void pathsMapToTheirSpecProduct(String path, String product) {
    assertEquals(product, TokenProducts.forPath(path));
  }

  @Test
  void operationsWithoutAProductHaveNone() {
    assertNull(TokenProducts.forPath("/v3/status/job-1"));
    assertNull(TokenProducts.forPath("/v3/services/bank_codes"));
  }
}
