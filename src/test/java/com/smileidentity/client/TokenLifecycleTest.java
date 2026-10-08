package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.errors.AuthenticationException;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Spec §2.3/§2A: one internal token is minted per authenticated request. */
class TokenLifecycleTest {

  private static final String ACCEPTED =
      "{\"status\":\"Accepted\",\"job_id\":\"job_x\",\"user_id\":\"user_x\"}";
  private static final String PROCESSING = "{\"status\":\"processing\",\"job_id\":\"job_x\"}";

  private MockWebServer server;
  private SmileID smile;

  @BeforeEach
  void setUp() throws Exception {
    server = TestSupport.tlsServer();
    smile = TestSupport.client(server);
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  private static String freshJwt() {
    return TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
  }

  private static UserDetails user() {
    return UserDetails.builder()
        .givenNames("John")
        .lastName("Doe")
        .email("john@example.com")
        .build();
  }

  private static Consent consent() {
    return Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy");
  }

  private void enhancedKyc(String userId) {
    smile
        .enhancedKyc()
        .verify(
            EnhancedKycParams.builder()
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .userDetails(user())
                .consent(consent())
                .userId(userId)
                .build());
  }

  private void enrolledBiometricKyc() {
    smile
        .biometricKyc()
        .verify(
            BiometricKycParams.builder()
                .useEnrolledImage(true)
                .userId("user-1")
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .userDetails(user())
                .consent(consent())
                .build());
  }

  private static Map<String, String> claims(RecordedRequest tokenRequest) {
    Map<String, String> claims = new LinkedHashMap<>();
    if (tokenRequest.getBodySize() == 0) {
      return claims;
    }
    for (TestPart part : MultipartParser.parse(tokenRequest)) {
      claims.put(part.name, part.body);
    }
    return claims;
  }

  @Test
  void tokenRequestUsesLowercaseHeadersNoBodyAndTelemetry() throws Exception {
    String jwt = freshJwt();
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.idStatusOk());

    smile.services().idStatus("NG", "NIN");

    RecordedRequest tokenReq = server.takeRequest();
    assertEquals("POST", tokenReq.getMethod());
    assertEquals("/v3/token", tokenReq.getPath());
    assertEquals(TestSupport.PARTNER_ID, tokenReq.getHeader("smileid-partner-id"));
    assertEquals(TestSupport.API_KEY, tokenReq.getHeader("smileid-api-key"));
    assertEquals(0, tokenReq.getBodySize());
    // Header-name quirk (§2.3): lowercase names as documented for /v3/token.
    List<String> names = TestSupport.exactHeaderNames(tokenReq);
    assertTrue(names.contains("smileid-partner-id"), "expected lowercase header, got " + names);
    assertTrue(names.contains("smileid-api-key"), "expected lowercase header, got " + names);
    // Telemetry on every request (§2.4).
    assertEquals("java", tokenReq.getHeader("SmileID-Source-SDK"));
    assertEquals("12.2.0", tokenReq.getHeader("SmileID-Source-SDK-Version"));
    assertNotNull(tokenReq.getHeader("User-Agent"));
    assertTrue(tokenReq.getHeader("User-Agent").startsWith("smileid-sdk-java/12.2.0 ("));

    RecordedRequest statusReq = server.takeRequest();
    assertEquals(jwt, statusReq.getHeader("SmileID-Token"));
    assertTrue(TestSupport.exactHeaderNames(statusReq).contains("SmileID-Token"));
    assertEquals("java", statusReq.getHeader("SmileID-Source-SDK"));
  }

  @Test
  void eachJobMintsItsOwnToken() throws Exception {
    for (int i = 0; i < 3; i++) {
      server.enqueue(TestSupport.tokenResponse(freshJwt()));
      server.enqueue(TestSupport.json(202, ACCEPTED));
    }

    enhancedKyc("user-1");
    enhancedKyc("user-1");
    enhancedKyc("user-1");

    assertEquals(6, server.getRequestCount());
    for (int i = 0; i < 3; i++) {
      assertEquals("/v3/token", server.takeRequest().getPath());
      assertEquals("/v3/enhanced_kyc", server.takeRequest().getPath());
    }
  }

  @Test
  void tokenBindsUserAndProduct() throws Exception {
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(TestSupport.json(202, ACCEPTED));

    enhancedKyc("user-1");

    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("user_id", "user-1");
    expected.put("product", "enhanced_kyc");
    assertEquals(expected, claims(server.takeRequest()));
  }

  @Test
  void tokenBindsBodyUserIdForAuthentication() throws Exception {
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(TestSupport.json(202, ACCEPTED));
    List<BinaryInput> liveness = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      liveness.add(BinaryInput.of("fake-liveness".getBytes(StandardCharsets.UTF_8)));
    }

    smile
        .biometric()
        .authenticate(
            AuthenticationParams.builder()
                .userId("user-1")
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .livenessImages(liveness)
                .userDetails(user())
                .consent(consent())
                .build());

    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("user_id", "user-1");
    expected.put("product", "smart_selfie_authentication");
    assertEquals(expected, claims(server.takeRequest()));
  }

  @Test
  void a401IsNotRetried() throws Exception {
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(
        TestSupport.json(
            401,
            "{\"status\":\"Unauthorized\",\"message\":\"Invalid authentication credentials.\"}"));

    AuthenticationException e =
        assertThrows(AuthenticationException.class, () -> enhancedKyc("user-1"));

    assertEquals(401, e.getStatusCode());
    assertEquals("Invalid authentication credentials.", e.getMessage());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void retriesReuseTheRequestToken() throws Exception {
    String jwt = freshJwt();
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.json(503, "{\"status\":\"error\",\"message\":\"busy\"}"));
    server.enqueue(TestSupport.json(200, PROCESSING));
    smile.transport().setSleeper(millis -> {});

    smile.verifications().retrieve("job_x");

    assertEquals(3, server.getRequestCount());
    assertEquals("/v3/token", server.takeRequest().getPath());
    assertEquals(jwt, server.takeRequest().getHeader("SmileID-Token"));
    assertEquals(jwt, server.takeRequest().getHeader("SmileID-Token"));
  }

  @Test
  void enrolledImageGrantIsBoundToTheBiometricKycToken() throws Exception {
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(TestSupport.json(202, ACCEPTED));

    enrolledBiometricKyc();

    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("user_id", "user-1");
    expected.put("product", "biometric_kyc");
    expected.put("use_enrolled_image", "true");
    assertEquals(expected, claims(server.takeRequest()));
    List<TestPart> jobParts = MultipartParser.parse(server.takeRequest());
    assertFalse(MultipartParser.has(jobParts, "use_enrolled_image"));
    assertFalse(MultipartParser.has(jobParts, "selfie_image"));
    assertFalse(MultipartParser.has(jobParts, "liveness_images"));
  }

  @Test
  void grantDoesNotReachLaterRequests() throws Exception {
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(TestSupport.json(202, ACCEPTED));
    server.enqueue(TestSupport.tokenResponse(freshJwt()));
    server.enqueue(TestSupport.json(200, PROCESSING));

    enrolledBiometricKyc();
    smile.verifications().retrieve("job_x");

    server.takeRequest();
    server.takeRequest();
    RecordedRequest laterToken = server.takeRequest();
    assertEquals("/v3/token", laterToken.getPath());
    assertTrue(claims(laterToken).isEmpty());
  }

  @Test
  void unauthenticatedServicesCallsNeverFetchOrSendAToken() throws Exception {
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"id_types\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"valid_documents\":[]}"));

    smile.services().bankCodes();
    smile.services().supportedIdTypes();
    smile.services().supportedDocuments();

    assertEquals(3, server.getRequestCount());
    for (int i = 0; i < 3; i++) {
      RecordedRequest r = server.takeRequest();
      assertNull(r.getHeader("SmileID-Token"), r.getPath() + " must not carry a token");
      assertTrue(r.getPath().startsWith("/v3/services/"));
      assertEquals("java", r.getHeader("SmileID-Source-SDK"));
    }
  }
}
