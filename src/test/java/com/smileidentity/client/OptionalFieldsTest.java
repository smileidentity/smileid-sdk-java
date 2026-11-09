package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.ReplayParams;
import com.smileidentity.generated.models.SupportedDocumentsParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.generated.operations.BiometricOperations;
import com.smileidentity.helpers.BinaryInput;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Optional fields and edge cases the golden fixtures leave out: every optional builder field
 * reaches the wire under its spec name, and absent optional values send nothing.
 */
class OptionalFieldsTest {

  private static final String ACCEPTED =
      "{\"status\":\"Accepted\",\"message\":\"Request accepted and queued for processing.\","
          + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"user_id\":\"user_01\"}";
  private static final String CALLBACK = "https://app.example.com/cb";
  private static final Map<String, String> PARTNER_PARAMS =
      Collections.singletonMap("order_id", "order-7");
  private static final List<MetadataEntry> METADATA =
      Collections.singletonList(new MetadataEntry("source", "sdk-test"));

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

  private void enqueueTokenAndAccepted() {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.json(202, ACCEPTED));
  }

  /** Skips the token request and returns the parts of the operation request. */
  private List<TestPart> operationParts(String expectedPath) throws Exception {
    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals(expectedPath, r.getPath());
    return MultipartParser.parse(r);
  }

  private static BinaryInput bytes(String content) {
    return BinaryInput.of(content.getBytes(StandardCharsets.UTF_8));
  }

  private static List<BinaryInput> liveness(int count) {
    List<BinaryInput> images = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      images.add(bytes("fake-liveness-" + i));
    }
    return images;
  }

  private static Consent consent() {
    return Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy");
  }

  private static UserDetails user() {
    return UserDetails.builder()
        .givenNames("John")
        .lastName("Doe")
        .email("john@example.com")
        .build();
  }

  private static void assertSharedOptionalParts(List<TestPart> parts) {
    assertEquals(CALLBACK, MultipartParser.single(parts, "callback_url").body);
    assertEquals(
        "{\"order_id\":\"order-7\"}", MultipartParser.single(parts, "partner_params").body);
    assertEquals(
        "[{\"name\":\"source\",\"value\":\"sdk-test\"}]",
        MultipartParser.single(parts, "metadata").body);
  }

  @Test
  void biometricKycOperationSendsEveryOptionalField() throws Exception {
    enqueueTokenAndAccepted();

    AcceptedResponse accepted =
        BiometricOperations.biometricKyc(
            smile.transport(),
            BiometricKycParams.builder()
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .userDetails(user())
                .consent(consent())
                .userId("user_01")
                .callbackUrl(CALLBACK)
                .sandboxResult(1)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build(),
            null);

    List<TestPart> parts = operationParts("/v3/biometric_kyc");
    assertSharedOptionalParts(parts);
    assertEquals("1", MultipartParser.single(parts, "sandbox_result").body);
    assertTrue(accepted.isAccepted());
  }

  @Test
  void enrollSendsEveryOptionalField() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .biometric()
        .enroll(
            EnrollParams.builder()
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .allowNewEnroll(false)
                .userDetails(user())
                .consent(consent())
                .userId("user_enroll_1")
                .callbackUrl(CALLBACK)
                .sandboxResult(2)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build());

    List<TestPart> parts = operationParts("/v3/registration");
    assertSharedOptionalParts(parts);
    assertEquals("false", MultipartParser.single(parts, "allow_new_enroll").body);
    assertEquals("2", MultipartParser.single(parts, "sandbox_result").body);
  }

  @Test
  void authenticateSendsEveryOptionalField() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .biometric()
        .authenticate(
            AuthenticationParams.builder()
                .userId("user_auth_001")
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .userDetails(user())
                .consent(consent())
                .callbackUrl(CALLBACK)
                .sandboxResult(0)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build());

    List<TestPart> parts = operationParts("/v3/authentication");
    assertSharedOptionalParts(parts);
    assertEquals("0", MultipartParser.single(parts, "sandbox_result").body);
  }

  @Test
  void compareSendsEveryOptionalFieldAndOmitsAnAbsentImageType() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .biometric()
        .compare(
            CompareParams.builder()
                .selfieImage(bytes("fake-selfie"))
                .comparisonImage(bytes("fake-comparison"))
                .livenessImages(liveness(3))
                .allowNewEnroll(true)
                .userId("user_compare_1")
                .userDetails(user())
                .consent(consent())
                .callbackUrl(CALLBACK)
                .sandboxResult(1)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build());

    List<TestPart> parts = operationParts("/v3/compare");
    assertSharedOptionalParts(parts);
    assertFalse(MultipartParser.has(parts, "comparison_image_type"));
    assertEquals(3, MultipartParser.byName(parts, "liveness_images").size());
    assertEquals("true", MultipartParser.single(parts, "allow_new_enroll").body);
    assertEquals("1", MultipartParser.single(parts, "sandbox_result").body);
  }

  @Test
  void documentVerificationSendsEveryOptionalField() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .documents()
        .verify(
            DocumentVerificationParams.builder()
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .document(bytes("fake-front"))
                .documentBack(bytes("fake-back"))
                .country("NG")
                .idType("PASSPORT")
                .userDetails(user())
                .consent(consent())
                .userId("user_01")
                .callbackUrl(CALLBACK)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build());

    List<TestPart> parts = operationParts("/v3/document_verification");
    assertSharedOptionalParts(parts);
    assertEquals("fake-back", MultipartParser.single(parts, "document_back").body);
  }

  @Test
  void enhancedDocumentVerificationSendsEveryOptionalField() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .documents()
        .verifyEnhanced(
            EnhancedDocumentVerificationParams.builder()
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .document(bytes("fake-front"))
                .documentBack(bytes("fake-back"))
                .country("NG")
                .idType("PASSPORT")
                .userDetails(user())
                .consent(consent())
                .userId("user_01")
                .callbackUrl(CALLBACK)
                .partnerParams(PARTNER_PARAMS)
                .metadata(METADATA)
                .build());

    List<TestPart> parts = operationParts("/v3/enhanced_document_verification");
    assertSharedOptionalParts(parts);
    assertEquals("fake-back", MultipartParser.single(parts, "document_back").body);
  }

  @Test
  void requestOptionsWithoutCallbackUrlFallBackToTheParamsValue() throws Exception {
    enqueueTokenAndAccepted();

    smile
        .biometric()
        .authenticate(
            AuthenticationParams.builder()
                .userId("user_auth_001")
                .selfieImage(bytes("fake-selfie"))
                .livenessImages(liveness(2))
                .userDetails(user())
                .consent(consent())
                .callbackUrl(CALLBACK)
                .build(),
            RequestOptions.builder().timeout(Duration.ofSeconds(30)).build());

    List<TestPart> parts = operationParts("/v3/authentication");
    assertEquals(CALLBACK, MultipartParser.single(parts, "callback_url").body);
  }

  @Test
  void replayWithParamsButNoCallbackUrlSendsNoBody() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.json(200, "{\"status\":\"ok\"}"));

    smile.verifications().replay("job_1", ReplayParams.builder().build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/replay/job_1", r.getPath());
    assertEquals(0, r.getBodySize());
    assertNull(r.getHeader("Content-Type"));
  }

  @Test
  void pathSegmentsEncodeEveryCharacterOutsideTheUnreservedSet() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.json(200, "{\"status\":\"clear\"}"));

    // Unreserved characters, then one character just outside each unreserved range.
    smile.verifications().retrieve("AZaz09-._~@[`{/: é");

    server.takeRequest();
    assertEquals(
        "/v3/status/AZaz09-._~%40%5B%60%7B%2F%3A%20%C3%A9", server.takeRequest().getPath());
  }

  @Test
  void supportedDocumentsWithEmptyParamsSendsNoQuery() throws Exception {
    server.enqueue(TestSupport.json(200, "{\"valid_documents\":[]}"));

    smile.services().supportedDocuments(SupportedDocumentsParams.builder().build());

    assertEquals("/v3/services/supported_documents", server.takeRequest().getPath());
  }
}
