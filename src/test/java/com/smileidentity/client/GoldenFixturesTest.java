package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.errors.ConflictException;
import com.smileidentity.errors.InvalidRequestException;
import com.smileidentity.errors.PaymentRequiredException;
import com.smileidentity.errors.PermissionException;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.ComparisonImageType;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.ReplayCallbackResponse;
import com.smileidentity.generated.models.ReplayParams;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.ReportUserFraudResponse;
import com.smileidentity.generated.models.SupportedDocumentsParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Spec §6 golden fixtures: for each of the 14 operations, the serialized request matches the spec
 * (field names, destinations, multipart structure, header routing) and the golden responses parse.
 */
class GoldenFixturesTest {

  private static final String GOLDEN_CONSENT_JSON =
      "{\"granted\":true,\"granted_at\":\"2026-03-06T12:00:00.000Z\",\"notice_language\":\"EN\","
          + "\"notice_privacy_policy_url\":\"https://example.com/privacy\"}";
  private static final String ACCEPTED_UPPER =
      "{\"status\":\"Accepted\",\"message\":\"Request accepted and queued for processing.\","
          + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\"}";
  private static final String ACCEPTED_LOWER =
      "{\"status\":\"accepted\",\"message\":\"Request accepted and queued for processing.\","
          + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
          + "\"created_at\":\"2026-03-10T12:00:00.000Z\"}";
  private static final String JOB_ID = "job_01h8x9y2z3a4b5c6d7e8f9g0h1";

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

  private void enqueueToken() {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
  }

  private static Consent goldenConsent() {
    return Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy");
  }

  private static UserDetails johnWithEmail() {
    return UserDetails.builder()
        .givenNames("John")
        .lastName("Doe")
        .email("john@example.com")
        .build();
  }

  private static UserDetails johnWithPhone() {
    return UserDetails.builder()
        .givenNames("John")
        .lastName("Doe")
        .phoneNumber("+2348012345678")
        .build();
  }

  private static List<BinaryInput> livenessImages(int count) {
    List<BinaryInput> images = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      images.add(BinaryInput.of(("fake-liveness-" + i).getBytes(StandardCharsets.UTF_8)));
    }
    return images;
  }

  // ---------------------------------------------------------------- 6.1 enhanced_kyc

  @Test
  void enhancedKycVerifyMatchesTheGoldenRequest() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    AcceptedResponse accepted =
        smile
            .enhancedKyc()
            .verify(
                EnhancedKycParams.builder()
                    .country("NG")
                    .idType("NIN")
                    .idNumber("12345678901")
                    .userDetails(johnWithEmail())
                    .consent(goldenConsent())
                    .userId("user_01h8x9y2z3a4b5c6d7e8f9g0h1")
                    .build());

    server.takeRequest(); // token
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/enhanced_kyc", r.getPath());
    assertEquals("user_01h8x9y2z3a4b5c6d7e8f9g0h1", r.getHeader("User-ID"));
    assertNotNull(r.getHeader("SmileID-Token"));
    assertNull(r.getHeader("SmileID-Partner-ID"), "no Partner-ID header on enhanced_kyc");

    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("NG", MultipartParser.single(parts, "country").body);
    assertEquals("NIN", MultipartParser.single(parts, "id_type").body);
    assertEquals("12345678901", MultipartParser.single(parts, "id_number").body);
    TestPart userDetails = MultipartParser.single(parts, "user_details");
    assertEquals("application/json", baseType(userDetails.contentType));
    assertEquals(
        "{\"given_names\":\"John\",\"last_name\":\"Doe\",\"email\":\"john@example.com\"}",
        userDetails.body);
    TestPart consent = MultipartParser.single(parts, "consent");
    assertEquals("application/json", baseType(consent.contentType));
    assertEquals(GOLDEN_CONSENT_JSON, consent.body);
    // Scalar parts have no content type on the wire.
    assertNull(MultipartParser.single(parts, "country").contentType);
    // No parts invented for absent optionals.
    assertFalse(MultipartParser.has(parts, "callback_url"));
    assertFalse(MultipartParser.has(parts, "user_id"));

    assertTrue(accepted.isAccepted());
    assertEquals(JOB_ID, accepted.getJobId());
  }

  @Test
  void enhancedKycOptionalFieldsAreSerializedWhenPresent() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    Map<String, String> partnerParams = new LinkedHashMap<>();
    partnerParams.put("order_ref", "ref-42");
    smile
        .enhancedKyc()
        .verify(
            EnhancedKycParams.builder()
                .country("NG")
                .idType("BANK_ACCOUNT")
                .idNumber("0123456789")
                .bankCode("044")
                .operator("MTN")
                .callbackUrl("https://app.example.com/cb")
                .partnerParams(partnerParams)
                .metadata(Arrays.asList(new MetadataEntry("source", "sdk-test")))
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals("044", MultipartParser.single(parts, "bank_code").body);
    assertEquals("MTN", MultipartParser.single(parts, "operator").body);
    assertEquals("https://app.example.com/cb", MultipartParser.single(parts, "callback_url").body);
    TestPart pp = MultipartParser.single(parts, "partner_params");
    assertEquals("application/json", baseType(pp.contentType));
    assertEquals("{\"order_ref\":\"ref-42\"}", pp.body);
    TestPart metadata = MultipartParser.single(parts, "metadata");
    assertEquals("application/json", baseType(metadata.contentType));
    assertEquals("[{\"name\":\"source\",\"value\":\"sdk-test\"}]", metadata.body);
  }

  @Test
  void defaultCallbackUrlIsUsedWhenACallOmitsCallbackUrl() throws Exception {
    SmileID withDefault =
        TestSupport.clientBuilder(server)
            .defaultCallbackUrl("https://app.example.com/default")
            .build();
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    withDefault
        .enhancedKyc()
        .verify(
            EnhancedKycParams.builder()
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals(
        "https://app.example.com/default", MultipartParser.single(parts, "callback_url").body);
  }

  @Test
  void requestOptionsCallbackUrlOverridesParamsAndDefault() throws Exception {
    SmileID withDefault =
        TestSupport.clientBuilder(server)
            .defaultCallbackUrl("https://app.example.com/default")
            .build();
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    withDefault
        .enhancedKyc()
        .verify(
            EnhancedKycParams.builder()
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .callbackUrl("https://app.example.com/from-params")
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build(),
            RequestOptions.builder().callbackUrl("https://app.example.com/override").build());

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals(
        "https://app.example.com/override", MultipartParser.single(parts, "callback_url").body);
  }

  // ---------------------------------------------------------------- 6.2 document_verification

  @Test
  void documentVerificationMatchesTheGoldenRequest() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_LOWER));

    AcceptedResponse accepted =
        smile
            .documents()
            .verify(
                DocumentVerificationParams.builder()
                    .selfieImage(
                        BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8))
                            .withFilename("selfie.jpg"))
                    .livenessImages(livenessImages(6))
                    .document(
                        BinaryInput.of("fake-document".getBytes(StandardCharsets.UTF_8))
                            .withFilename("doc.jpg"))
                    .country("NG")
                    .userDetails(johnWithPhone())
                    .consent(goldenConsent())
                    .userId("user_01h8x9y2z3a4b5c6d7e8f9g0h1")
                    .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/document_verification", r.getPath());
    assertEquals(TestSupport.PARTNER_ID, r.getHeader("SmileID-Partner-ID"));
    assertEquals("user_01h8x9y2z3a4b5c6d7e8f9g0h1", r.getHeader("User-ID"));

    List<TestPart> parts = MultipartParser.parse(r);
    TestPart selfie = MultipartParser.single(parts, "selfie_image");
    assertEquals("image/jpeg", baseType(selfie.contentType));
    assertEquals("selfie.jpg", selfie.filename);
    assertEquals("fake-selfie", selfie.body);

    // Repeated liveness_images parts, same field name, never CSV or indexed (§5.3.4).
    List<TestPart> liveness = MultipartParser.byName(parts, "liveness_images");
    assertEquals(6, liveness.size());
    for (TestPart frame : liveness) {
      assertEquals("image/jpeg", baseType(frame.contentType));
      assertNotNull(frame.filename);
    }
    assertEquals("fake-liveness-1", liveness.get(0).body);
    assertEquals("fake-liveness-6", liveness.get(5).body);
    assertFalse(MultipartParser.has(parts, "liveness_images[0]"));
    assertFalse(MultipartParser.has(parts, "liveness_images[]"));

    TestPart document = MultipartParser.single(parts, "document");
    assertEquals("image/jpeg", baseType(document.contentType));
    assertEquals("doc.jpg", document.filename);
    assertEquals("NG", MultipartParser.single(parts, "country").body);
    assertFalse(MultipartParser.has(parts, "id_type"), "id_type omitted means auto-classify");
    assertFalse(MultipartParser.has(parts, "document_back"));
    assertEquals(
        "{\"given_names\":\"John\",\"last_name\":\"Doe\",\"phone_number\":\"+2348012345678\"}",
        MultipartParser.single(parts, "user_details").body);
    assertEquals(GOLDEN_CONSENT_JSON, MultipartParser.single(parts, "consent").body);

    assertTrue(accepted.isAccepted(), "lowercase 'accepted' must normalize");
    assertEquals("2026-03-10T12:00:00.000Z", accepted.getCreatedAt());
  }

  @Test
  void documentBackSupportsPngContentType() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_LOWER));

    smile
        .documents()
        .verify(
            DocumentVerificationParams.builder()
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .livenessImages(livenessImages(6))
                .document(
                    BinaryInput.of(
                            new ByteArrayInputStream("front-png".getBytes(StandardCharsets.UTF_8)))
                        .withContentType("image/png")
                        .withFilename("front.png"))
                .documentBack(
                    BinaryInput.of("back-png".getBytes(StandardCharsets.UTF_8))
                        .withContentType("image/png")
                        .withFilename("back.png"))
                .country("NG")
                .idType("PASSPORT")
                .userDetails(johnWithPhone())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals("image/png", baseType(MultipartParser.single(parts, "document").contentType));
    assertEquals("front-png", MultipartParser.single(parts, "document").body);
    TestPart back = MultipartParser.single(parts, "document_back");
    assertEquals("image/png", baseType(back.contentType));
    assertEquals("back.png", back.filename);
    assertEquals("PASSPORT", MultipartParser.single(parts, "id_type").body);
  }

  // ------------------------------------------- 6.3 enhanced_document_verification

  @Test
  void enhancedDocumentVerificationRequiresPartnerIdHeaderAndIdType() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_LOWER));

    smile
        .documents()
        .verifyEnhanced(
            EnhancedDocumentVerificationParams.builder()
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .livenessImages(livenessImages(7))
                .document(BinaryInput.of("fake-document".getBytes(StandardCharsets.UTF_8)))
                .country("NG")
                .idType("PASSPORT")
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/enhanced_document_verification", r.getPath());
    assertEquals(TestSupport.PARTNER_ID, r.getHeader("SmileID-Partner-ID"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("PASSPORT", MultipartParser.single(parts, "id_type").body);
    assertEquals(7, MultipartParser.byName(parts, "liveness_images").size());
  }

  // ---------------------------------------------------------------- 6.4 biometric_kyc

  @Test
  void biometricKycMatchesTheGoldenContract() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_LOWER));

    smile
        .biometricKyc()
        .verify(
            BiometricKycParams.builder()
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .livenessImages(livenessImages(6))
                .country("NG")
                .idType("NIN")
                .idNumber("12345678901")
                .sandboxResult(0)
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .userId("user_01h8x9y2z3a4b5c6d7e8f9g0h1")
                .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/biometric_kyc", r.getPath());
    assertEquals(TestSupport.PARTNER_ID, r.getHeader("SmileID-Partner-ID"));
    assertEquals("user_01h8x9y2z3a4b5c6d7e8f9g0h1", r.getHeader("User-ID"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("NIN", MultipartParser.single(parts, "id_type").body);
    assertEquals("12345678901", MultipartParser.single(parts, "id_number").body);
    assertEquals("0", MultipartParser.single(parts, "sandbox_result").body);
    assertEquals(6, MultipartParser.byName(parts, "liveness_images").size());
  }

  // ---------------------------------------------------------------- 6.5 registration

  @Test
  void biometricEnrollPostsToRegistrationWithoutPartnerIdHeader() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    AcceptedResponse accepted =
        smile
            .biometric()
            .enroll(
                EnrollParams.builder()
                    .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                    .livenessImages(livenessImages(8))
                    .allowNewEnroll(true)
                    .userDetails(johnWithEmail())
                    .consent(goldenConsent())
                    .userId("user_enroll_1")
                    .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/registration", r.getPath());
    assertNull(r.getHeader("SmileID-Partner-ID"));
    assertEquals("user_enroll_1", r.getHeader("User-ID"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("true", MultipartParser.single(parts, "allow_new_enroll").body);
    assertEquals(8, MultipartParser.byName(parts, "liveness_images").size());
    assertFalse(MultipartParser.has(parts, "user_id"), "user_id travels in the User-ID header");
    assertTrue(accepted.isAccepted());
  }

  // ---------------------------------------------------------------- 6.6 authentication

  @Test
  void biometricAuthenticateSendsUserIdInTheBodyNotTheHeader() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    smile
        .biometric()
        .authenticate(
            AuthenticationParams.builder()
                .userId("user_auth_001")
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .livenessImages(livenessImages(6))
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/authentication", r.getPath());
    assertNull(r.getHeader("User-ID"), "authentication routes user_id to the body (§6.6)");
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("user_auth_001", MultipartParser.single(parts, "user_id").body);
    assertEquals(6, MultipartParser.byName(parts, "liveness_images").size());
  }

  @Test
  void biometricAuthenticateWithEnrolledImageSkipsImages() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    smile
        .biometric()
        .authenticate(
            AuthenticationParams.builder()
                .userId("user_auth_001")
                .useEnrolledImage(true)
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals("true", MultipartParser.single(parts, "use_enrolled_image").body);
    assertFalse(MultipartParser.has(parts, "selfie_image"));
    assertFalse(MultipartParser.has(parts, "liveness_images"));
  }

  // ---------------------------------------------------------------- 6.7 compare

  @Test
  void biometricCompareMatchesTheGoldenContract() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED_UPPER));

    smile
        .biometric()
        .compare(
            CompareParams.builder()
                .selfieImage(BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8)))
                .comparisonImage(
                    BinaryInput.of("fake-comparison".getBytes(StandardCharsets.UTF_8))
                        .withFilename("comparison.jpg"))
                .comparisonImageType(ComparisonImageType.ID_PHOTO)
                .userId("user_compare_1")
                .userDetails(johnWithEmail())
                .consent(goldenConsent())
                .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("/v3/compare", r.getPath());
    assertNull(r.getHeader("User-ID"), "compare routes user_id to the body (§6.7)");
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("ID_PHOTO", MultipartParser.single(parts, "comparison_image_type").body);
    TestPart comparison = MultipartParser.single(parts, "comparison_image");
    assertEquals("image/jpeg", baseType(comparison.contentType));
    assertEquals("comparison.jpg", comparison.filename);
    assertEquals("user_compare_1", MultipartParser.single(parts, "user_id").body);
    assertFalse(MultipartParser.has(parts, "liveness_images"), "liveness optional on compare");
  }

  // ---------------------------------------------------------------- 6.8 status

  @Test
  void verificationsRetrieveIsAnAuthenticatedGet() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            200,
            "{\"status\":\"complete\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"message\":\"Verification completed with state: clear\"}"));

    JobStatus status = smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("GET", r.getMethod());
    assertEquals("/v3/status/job_01h2xcejqtf2nbrexx3vqjhp41", r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertTrue(status.isComplete());
    assertEquals("Verification completed with state: clear", status.getMessage());
  }

  @Test
  void verificationsRetrieveParsesProcessing202() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"processing\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"message\":\"Verification is still processing\"}"));
    JobStatus status = smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");
    assertTrue(status.isProcessing());
  }

  @Test
  void verificationsRetrieveReturnsNotFoundJobStatusInsteadOfRaising() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            404,
            "{\"status\":\"not_found\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"unknown\",\"message\":\"Verification not found\"}"));
    JobStatus status = smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");
    assertTrue(status.isNotFound());
    assertEquals("unknown", status.getUserId());
  }

  // ---------------------------------------------------------------- 6.10 replay

  @Test
  void replayWithOverrideSendsMultipartWithOneCallbackUrlPart() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"test-user\",\"message\":\"Callback replay queued successfully.\"}"));

    ReplayCallbackResponse response =
        smile
            .verifications()
            .replay(
                "job_01h2xcejqtf2nbrexx3vqjhp41",
                ReplayParams.builder().callbackUrl("https://app.example.com/cb").build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/replay/job_01h2xcejqtf2nbrexx3vqjhp41", r.getPath());
    // Corrected contract: the backend takes multipart/form-data, not JSON.
    assertTrue(
        r.getHeader("Content-Type").startsWith("multipart/form-data"),
        "replay body is multipart, got " + r.getHeader("Content-Type"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals(1, parts.size(), "exactly one part");
    TestPart callback = MultipartParser.single(parts, "callback_url");
    assertEquals("https://app.example.com/cb", callback.body);
    assertNull(callback.contentType, "scalar text part has no content type");
    assertEquals("accepted", response.getStatus());
    assertEquals("test-user", response.getUserId());
  }

  @Test
  void replayWithoutOverrideSendsNoBody() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"test-user\",\"message\":\"Callback replay queued successfully.\"}"));
    smile.verifications().replay("job_01h2xcejqtf2nbrexx3vqjhp41");
    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    // OkHttp requires a RequestBody on POST, so this is a zero-length body with no Content-Type.
    assertEquals(0, r.getBodySize());
    assertNull(r.getHeader("Content-Type"));
  }

  @Test
  void replayConflictRaisesConflictErrorAndIsNeverRetried() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            409,
            "{\"status\":\"Conflict\",\"message\":\"Verification is still processing. "
                + "Callbacks can only be replayed for completed verifications.\"}"));

    ConflictException e =
        assertThrows(
            ConflictException.class,
            () -> smile.verifications().replay("job_01h2xcejqtf2nbrexx3vqjhp41"));
    assertEquals(409, e.getStatusCode());
    assertEquals(2, server.getRequestCount(), "409 must not be retried");
  }

  // ---------------------------------------------------------------- 6.11 report_fraud

  @Test
  void reportFraudIsMultipartWithConditionalFields() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"message\":\"Fraud report accepted\",\"user_id\":\"user-123\"}"));

    ReportUserFraudResponse response =
        smile
            .users()
            .reportFraud(
                "user-123",
                ReportFraudParams.builder()
                    .isFraud(true)
                    .reason(FraudReason.ACCOUNT_TAKEOVER)
                    .notes("Suspicious takeover pattern")
                    .reportedBy("fraud-team@example.com")
                    .build());

    server.takeRequest();
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/users/user-123/report_fraud", r.getPath());
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals("true", MultipartParser.single(parts, "is_fraud").body);
    assertEquals("ACCOUNT_TAKEOVER", MultipartParser.single(parts, "reason").body);
    assertEquals("Suspicious takeover pattern", MultipartParser.single(parts, "notes").body);
    assertEquals("fraud-team@example.com", MultipartParser.single(parts, "reported_by").body);
    assertEquals("accepted", response.getStatus());
    assertEquals("user-123", response.getUserId());
  }

  @Test
  void flagFraudWrapperSetsIsFraudTrue() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"message\":\"Fraud report accepted\",\"user_id\":\"user-123\"}"));

    smile
        .users()
        .flagFraud("user-123", FraudReason.DOCUMENT_FORGERY, null, "fraud-team@example.com");

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals("true", MultipartParser.single(parts, "is_fraud").body);
    assertEquals("DOCUMENT_FORGERY", MultipartParser.single(parts, "reason").body);
    assertFalse(MultipartParser.has(parts, "notes"));
  }

  @Test
  void clearFraudWrapperSetsIsFraudFalseWithNotes() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"message\":\"Fraud report accepted\",\"user_id\":\"user-123\"}"));

    smile.users().clearFraud("user-123", "False positive after review", "fraud-team@example.com");

    server.takeRequest();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals("false", MultipartParser.single(parts, "is_fraud").body);
    assertEquals("False positive after review", MultipartParser.single(parts, "notes").body);
    assertFalse(MultipartParser.has(parts, "reason"));
  }

  // ---------------------------------------------------------------- 6.12–6.15 services

  @Test
  void servicesQueriesCarryTheirQueryParameters() throws Exception {
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"id_types\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"valid_documents\":[]}"));
    enqueueToken();
    server.enqueue(TestSupport.idStatusOk());

    smile.services().bankCodes("NG");
    smile.services().supportedIdTypes("NG");
    smile
        .services()
        .supportedDocuments(
            SupportedDocumentsParams.builder()
                .continent("AFRICA")
                .countryCode("NG")
                .locale("en-GB")
                .build());
    smile.services().idStatus("NG", "NIN");

    assertEquals("/v3/services/bank_codes?country=NG", server.takeRequest().getPath());
    assertEquals("/v3/services/supported_id_types?country=NG", server.takeRequest().getPath());
    assertEquals(
        "/v3/services/supported_documents?continent=AFRICA&country_code=NG&locale=en-GB",
        server.takeRequest().getPath());
    server.takeRequest(); // token
    RecordedRequest idStatus = server.takeRequest();
    assertEquals("/v3/services/id_status?country=NG&id_type=NIN", idStatus.getPath());
    assertNotNull(idStatus.getHeader("SmileID-Token"));
  }

  // ---------------------------------------------------------------- §7 through the transport

  @Test
  void servicesErrorCodeShapeRaisesPermissionErrorWithCode() throws Exception {
    server.enqueue(
        TestSupport.json(
            403, "{\"error\":\"You are not authorized to do that.\",\"code\":\"2413\"}"));
    PermissionException e =
        assertThrows(PermissionException.class, () -> smile.services().bankCodes());
    assertEquals(403, e.getStatusCode());
    assertEquals("2413", e.getCode());
    assertEquals("You are not authorized to do that.", e.getMessage());
  }

  @Test
  void idStatusReorderedErrorShapeRaisesInvalidRequest() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            400, "{\"message\":\"\\\"country\\\" is required\",\"status\":\"Bad Request\"}"));
    InvalidRequestException e =
        assertThrows(InvalidRequestException.class, () -> smile.services().idStatus("", "NIN"));
    assertEquals("\"country\" is required", e.getMessage());
    assertEquals("Bad Request", e.getStatus());
  }

  @Test
  void entry402RaisesPaymentRequired() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            402, "{\"status\":\"Payment Required\",\"message\":\"Insufficient wallet balance.\"}"));
    PaymentRequiredException e =
        assertThrows(
            PaymentRequiredException.class,
            () ->
                smile
                    .enhancedKyc()
                    .verify(
                        EnhancedKycParams.builder()
                            .country("NG")
                            .idType("NIN")
                            .idNumber("12345678901")
                            .userDetails(johnWithEmail())
                            .consent(goldenConsent())
                            .build()));
    assertInstanceOf(PaymentRequiredException.class, e);
    assertEquals(402, e.getStatusCode());
    assertEquals("Insufficient wallet balance.", e.getMessage());
  }

  private static String baseType(String contentType) {
    if (contentType == null) {
      return null;
    }
    int semicolon = contentType.indexOf(';');
    return (semicolon >= 0 ? contentType.substring(0, semicolon) : contentType).trim();
  }
}
