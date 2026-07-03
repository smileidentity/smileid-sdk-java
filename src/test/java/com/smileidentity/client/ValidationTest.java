package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smileidentity.errors.InvalidRequestException;
import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Client-side validation (spec §5.1, §6.6, §6.11): raised locally before any request is sent, so
 * the mock server must see zero requests (not even a token fetch).
 */
class ValidationTest {

  private MockWebServer server;
  private SmileID smile;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    smile = TestSupport.client(server);
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  private static Consent consent() {
    return Consent.granted(Instant.now(), "EN", "https://example.com/privacy");
  }

  private static UserDetails noContactDetails() {
    return UserDetails.builder().givenNames("John").lastName("Doe").build();
  }

  private static UserDetails validUserDetails() {
    return UserDetails.builder()
        .givenNames("John")
        .lastName("Doe")
        .email("john@example.com")
        .build();
  }

  private static List<BinaryInput> liveness(int count) {
    List<BinaryInput> images = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      images.add(BinaryInput.of(new byte[] {1, 2, 3}));
    }
    return images;
  }

  @Test
  void configRejectsUnsafeBaseUrl() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            SmileID.builder()
                .partnerId("1234")
                .apiKey("key")
                .baseUrl("http://api.example.com")
                .build());
  }

  @Test
  void configAllowsExplicitInsecureLoopbackBaseUrl() {
    SmileID.builder()
        .partnerId("1234")
        .apiKey("key")
        .baseUrl("http://localhost:8080")
        .allowInsecureBaseUrl(true)
        .build();
  }

  @Test
  void configRejectsInsecureDefaultCallbackUrl() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            SmileID.builder()
                .partnerId("1234")
                .apiKey("key")
                .defaultCallbackUrl("http://partner.example.com/webhook")
                .build());
  }

  @Test
  void userDetailsWithoutEmailOrPhoneIsRejectedBeforeSending() {
    ValidationException e =
        assertThrows(
            ValidationException.class,
            () ->
                smile
                    .enhancedKyc()
                    .verify(
                        EnhancedKycParams.builder()
                            .country("NG")
                            .idType("NIN")
                            .idNumber("12345678901")
                            .userDetails(noContactDetails())
                            .consent(consent())
                            .build()));
    assertInstanceOf(InvalidRequestException.class, e);
    assertNull(e.getStatusCode(), "local error: no HTTP status");
    assertEquals(0, server.getRequestCount(), "nothing may be sent, not even a token fetch");
  }

  @Test
  void enhancedDocumentVerificationRequiresIdTypeBeforeSending() {
    java.util.List<com.smileidentity.helpers.BinaryInput> liveness = new java.util.ArrayList<>();
    for (int i = 0; i < 6; i++) {
      liveness.add(com.smileidentity.helpers.BinaryInput.of(new byte[] {1, 2, 3}));
    }
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .documents()
                .verifyEnhanced(
                    EnhancedDocumentVerificationParams.builder()
                        .selfieImage(com.smileidentity.helpers.BinaryInput.of(new byte[] {1}))
                        .livenessImages(liveness)
                        .document(com.smileidentity.helpers.BinaryInput.of(new byte[] {1}))
                        .country("NG")
                        .userDetails(
                            UserDetails.builder()
                                .givenNames("John")
                                .lastName("Doe")
                                .email("john@example.com")
                                .build())
                        .consent(consent())
                        .build()));
    assertEquals(0, server.getRequestCount(), "id_type is enforced client-side (spec §6.3)");
  }

  @Test
  void livenessImageCountMustBeSixToEight() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .documents()
                .verify(
                    DocumentVerificationParams.builder()
                        .selfieImage(BinaryInput.of(new byte[] {1}))
                        .livenessImages(liveness(5))
                        .document(BinaryInput.of(new byte[] {1}))
                        .country("NG")
                        .userDetails(validUserDetails())
                        .consent(consent())
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void callbackUrlMustUseHttps() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .enhancedKyc()
                .verify(
                    EnhancedKycParams.builder()
                        .country("NG")
                        .idType("NIN")
                        .idNumber("12345678901")
                        .userDetails(validUserDetails())
                        .consent(consent())
                        .callbackUrl("http://partner.example.com/webhook")
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void pathParametersAreEncodedAsSingleSegments() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(
        TestSupport.json(
            200, "{\"status\":\"complete\",\"job_id\":\"job/with/slash\",\"user_id\":\"user\"}"));

    smile.verifications().retrieve("job/with/slash");

    server.takeRequest();
    assertEquals("/v3/status/job%2Fwith%2Fslash", server.takeRequest().getPath());
  }

  @Test
  void missingUserDetailsIsRejectedBeforeSending() {
    assertThrows(
        ValidationException.class,
        () -> smile.biometric().enroll(EnrollParams.builder().consent(consent()).build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void authenticationRequiresUserId() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .biometric()
                .authenticate(
                    AuthenticationParams.builder()
                        .userDetails(
                            UserDetails.builder()
                                .givenNames("John")
                                .lastName("Doe")
                                .email("john@example.com")
                                .build())
                        .consent(consent())
                        .useEnrolledImage(true)
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void authenticationRequiresImagesUnlessUseEnrolledImage() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .biometric()
                .authenticate(
                    AuthenticationParams.builder()
                        .userId("user_auth_001")
                        .userDetails(
                            UserDetails.builder()
                                .givenNames("John")
                                .lastName("Doe")
                                .email("john@example.com")
                                .build())
                        .consent(consent())
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void reportFraudTrueRequiresReason() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .users()
                .reportFraud(
                    "user-123",
                    ReportFraudParams.builder()
                        .isFraud(true)
                        .reportedBy("fraud-team@example.com")
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void reportFraudFalseRequiresNotes() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .users()
                .reportFraud(
                    "user-123",
                    ReportFraudParams.builder()
                        .isFraud(false)
                        .reportedBy("fraud-team@example.com")
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void reportFraudReasonOtherRequiresNotes() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .users()
                .reportFraud(
                    "user-123",
                    ReportFraudParams.builder()
                        .isFraud(true)
                        .reason(FraudReason.OTHER)
                        .reportedBy("fraud-team@example.com")
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void reportFraudNotesOver500CharactersAreRejected() {
    StringBuilder longNotes = new StringBuilder();
    for (int i = 0; i < 501; i++) {
      longNotes.append('x');
    }
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .users()
                .reportFraud(
                    "user-123",
                    ReportFraudParams.builder()
                        .isFraud(false)
                        .notes(longNotes.toString())
                        .reportedBy("fraud-team@example.com")
                        .build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void reportFraudRequiresReportedBy() {
    assertThrows(
        ValidationException.class,
        () ->
            smile
                .users()
                .reportFraud(
                    "user-123",
                    ReportFraudParams.builder()
                        .isFraud(true)
                        .reason(FraudReason.ACCOUNT_TAKEOVER)
                        .build()));
    assertEquals(0, server.getRequestCount());
  }
}
