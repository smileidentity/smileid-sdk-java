package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.ReplayParams;
import com.smileidentity.generated.models.UserDetails;
import java.time.Instant;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fleet standard (2026-07-03): base_url must be an absolute https URL with no query or fragment (no
 * escape hatch), and every callback URL must be https — validated at construction for the default
 * and before send for per-request values.
 */
class HttpsEnforcementTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = TestSupport.tlsServer();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  private static SmileID.Builder minimalBuilder() {
    return SmileID.builder().partnerId("1234").apiKey("fake-api-key-for-tests");
  }

  @Test
  void httpBaseUrlIsRejectedAtConstruction() {
    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> minimalBuilder().baseUrl("http://testapi.smileidentity.com").build());
    assertNotNull(e.getMessage());
  }

  @Test
  void baseUrlWithQueryOrFragmentIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> minimalBuilder().baseUrl("https://testapi.smileidentity.com?a=b").build());
    assertThrows(
        IllegalArgumentException.class,
        () -> minimalBuilder().baseUrl("https://testapi.smileidentity.com#frag").build());
  }

  @Test
  void relativeOrGarbageBaseUrlIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> minimalBuilder().baseUrl("/v3").build());
    assertThrows(
        IllegalArgumentException.class, () -> minimalBuilder().baseUrl("not a url").build());
  }

  @Test
  void httpsBaseUrlIsAccepted() {
    minimalBuilder().baseUrl("https://testapi.smileidentity.com").build();
    minimalBuilder().build(); // environment default is https
  }

  @Test
  void httpDefaultCallbackUrlIsRejectedAtConstruction() {
    assertThrows(
        ValidationException.class,
        () -> minimalBuilder().defaultCallbackUrl("http://app.example.com/cb").build());
  }

  @Test
  void httpsDefaultCallbackUrlIsAccepted() {
    minimalBuilder().defaultCallbackUrl("https://app.example.com/cb").build();
  }

  @Test
  void httpParamsCallbackUrlIsRejectedBeforeSend() {
    SmileID smile = TestSupport.client(server);
    assertThrows(
        ValidationException.class,
        () -> smile.enhancedKyc().verify(kycParams("http://app.example.com/cb")));
    assertEquals(0, server.getRequestCount(), "no request may be made, not even a token fetch");
  }

  @Test
  void httpRequestOptionsCallbackOverrideIsRejectedBeforeSend() {
    SmileID smile = TestSupport.client(server);
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .enhancedKyc()
                .verify(
                    kycParams(null),
                    RequestOptions.builder().callbackUrl("http://app.example.com/cb").build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void httpReplayCallbackUrlIsRejectedBeforeSend() {
    SmileID smile = TestSupport.client(server);
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .verifications()
                .replay(
                    "job_01h2xcejqtf2nbrexx3vqjhp41",
                    ReplayParams.builder().callbackUrl("http://app.example.com/cb").build()));
    assertEquals(0, server.getRequestCount());
  }

  private static EnhancedKycParams kycParams(String callbackUrl) {
    EnhancedKycParams.Builder builder =
        EnhancedKycParams.builder()
            .country("NG")
            .idType("NIN")
            .idNumber("12345678901")
            .userDetails(
                UserDetails.builder()
                    .givenNames("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .build())
            .consent(Consent.granted(Instant.now(), "EN", "https://example.com/privacy"));
    if (callbackUrl != null) {
      builder.callbackUrl(callbackUrl);
    }
    return builder.build();
  }
}
