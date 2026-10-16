package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.errors.ApiException;
import com.smileidentity.errors.AuthenticationException;
import com.smileidentity.errors.ConnectionException;
import com.smileidentity.errors.UnexpectedResponseException;
import com.smileidentity.errors.ValidationException;
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
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.ResidencyDocumentVerificationParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.generated.operations.BiometricOperations;
import com.smileidentity.helpers.BinaryInput;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Spec §2.3/§2A: one internal token is minted per authenticated request. */
class TokenLifecycleTest {

  private static final String ACCEPTED =
      "{\"status\":\"Accepted\",\"job_id\":\"job_x\",\"user_id\":\"user_x\"}";
  private static final String PROCESSING = "{\"status\":\"processing\",\"job_id\":\"job_x\"}";

  private MockWebServer server;
  private SmileID smile;
  private int mints;

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
    server.enqueue(TestSupport.tokenResponse("token-" + ++mints));
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

  private static BinaryInput image() {
    return BinaryInput.of("fake-image".getBytes(StandardCharsets.UTF_8));
  }

  private static List<BinaryInput> liveness() {
    List<BinaryInput> liveness = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      liveness.add(image());
    }
    return liveness;
  }

  private void enhancedKyc(String userId) {
    smile.enhancedKyc().verify(enhancedKycParams(userId));
  }

  private static EnhancedKycParams enhancedKycParams(String userId) {
    return EnhancedKycParams.builder()
        .country("NG")
        .idType("NIN")
        .idNumber("12345678901")
        .userDetails(user())
        .consent(consent())
        .userId(userId)
        .build();
  }

  private static BiometricKycParams.Builder biometricKycParams() {
    return BiometricKycParams.builder()
        .userId("user-1")
        .country("NG")
        .idType("NIN")
        .idNumber("12345678901")
        .userDetails(user())
        .consent(consent());
  }

  private void enrolledBiometricKyc() {
    smile.biometricKyc().verifyWithEnrolledImage(biometricKycParams().build());
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

  private static Map<String, String> claims(String... keyValues) {
    Map<String, String> claims = new LinkedHashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      claims.put(keyValues[i], keyValues[i + 1]);
    }
    return claims;
  }

  private static Arguments operation(String name, String path, Consumer<SmileID> call) {
    return Arguments.of(name, path, call);
  }

  private static Stream<Arguments> jobOperations() {
    return Stream.of(
        operation(
            "enhanced_kyc",
            "/v3/enhanced_kyc",
            c -> c.enhancedKyc().verify(enhancedKycParams("user-1"))),
        operation(
            "document_verification",
            "/v3/document_verification",
            c ->
                c.documents()
                    .verify(
                        DocumentVerificationParams.builder()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .document(image())
                            .country("NG")
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())),
        operation(
            "enhanced_document_verification",
            "/v3/enhanced_document_verification",
            c ->
                c.documents()
                    .verifyEnhanced(
                        EnhancedDocumentVerificationParams.builder()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .document(image())
                            .country("NG")
                            .idType("PASSPORT")
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())),
        operation(
            "residency_document_verification",
            "/v3/residency_document_verification",
            c ->
                c.documents()
                    .verifyResidency(
                        ResidencyDocumentVerificationParams.builder()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .document(image())
                            .visa(image())
                            .country("ZA")
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())),
        operation(
            "biometric_kyc",
            "/v3/biometric_kyc",
            c ->
                c.biometricKyc()
                    .verify(
                        biometricKycParams()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .build())),
        operation(
            "smart_selfie_registration",
            "/v3/registration",
            c ->
                c.biometric()
                    .enroll(
                        EnrollParams.builder()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())),
        operation(
            "smart_selfie_authentication",
            "/v3/authentication",
            c ->
                c.biometric()
                    .authenticate(
                        AuthenticationParams.builder()
                            .selfieImage(image())
                            .livenessImages(liveness())
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())),
        operation(
            "smart_selfie_compare",
            "/v3/compare",
            c ->
                c.biometric()
                    .compare(
                        CompareParams.builder()
                            .selfieImage(image())
                            .comparisonImage(image())
                            .comparisonImageType(ComparisonImageType.ID_PHOTO)
                            .userDetails(user())
                            .consent(consent())
                            .userId("user-1")
                            .build())));
  }

  private static Stream<Arguments> unboundOperations() {
    return Stream.of(
        operation("status", "/v3/status/job_x", c -> c.verifications().retrieve("job_x")),
        operation("replay", "/v3/replay/job_x", c -> c.verifications().replay("job_x")),
        operation(
            "report_fraud",
            "/v3/users/user-1/report_fraud",
            c ->
                c.users()
                    .reportFraud(
                        "user-1",
                        ReportFraudParams.builder()
                            .isFraud(true)
                            .reason(FraudReason.ACCOUNT_TAKEOVER)
                            .reportedBy("risk@partner.example")
                            .build())),
        operation("id_status", "/v3/services/id_status", c -> c.services().idStatus("NG", "NIN")));
  }

  @Test
  void tokenRequestUsesLowercaseHeadersNoBodyAndTelemetry() throws Exception {
    enqueueToken();
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
    assertEquals("token-1", statusReq.getHeader("SmileID-Token"));
    assertTrue(TestSupport.exactHeaderNames(statusReq).contains("SmileID-Token"));
    assertEquals("java", statusReq.getHeader("SmileID-Source-SDK"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("jobOperations")
  void jobTokenBindsItsUserAndProduct(String product, String path, Consumer<SmileID> call)
      throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED));

    call.accept(smile);

    assertEquals(claims("user_id", "user-1", "product", product), claims(server.takeRequest()));
    assertEquals(path, server.takeRequest().getPath());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unboundOperations")
  void nonJobTokenBindsNothing(String name, String path, Consumer<SmileID> call) throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, PROCESSING));

    call.accept(smile);

    RecordedRequest tokenReq = server.takeRequest();
    assertEquals("/v3/token", tokenReq.getPath());
    assertEquals(0, tokenReq.getBodySize());
    assertEquals(path, server.takeRequest().getRequestUrl().encodedPath());
  }

  @Test
  void jobWithoutUserBindsOnlyProduct() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED));

    enhancedKyc(null);

    assertEquals(claims("product", "enhanced_kyc"), claims(server.takeRequest()));
  }

  @Test
  void eachJobSendsTheTokenMintedForIt() throws Exception {
    for (int i = 0; i < 3; i++) {
      enqueueToken();
      server.enqueue(TestSupport.json(202, ACCEPTED));
    }

    enhancedKyc("user-1");
    enhancedKyc("user-1");
    enhancedKyc("user-1");

    assertEquals(6, server.getRequestCount());
    for (int i = 1; i <= 3; i++) {
      assertEquals("/v3/token", server.takeRequest().getPath());
      assertEquals("token-" + i, server.takeRequest().getHeader("SmileID-Token"));
    }
  }

  @Test
  void a401IsNotRetried() {
    enqueueToken();
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
    enqueueToken();
    server.enqueue(TestSupport.json(503, "{\"status\":\"error\",\"message\":\"busy\"}"));
    server.enqueue(TestSupport.json(200, PROCESSING));
    smile.transport().setSleeper(millis -> {});

    smile.verifications().retrieve("job_x");

    assertEquals(3, server.getRequestCount());
    assertEquals("/v3/token", server.takeRequest().getPath());
    assertEquals("token-1", server.takeRequest().getHeader("SmileID-Token"));
    assertEquals("token-1", server.takeRequest().getHeader("SmileID-Token"));
  }

  @Test
  void a401OnTheTokenRequestNeverSendsTheJob() {
    server.enqueue(TestSupport.json(401, "{\"status\":\"Unauthorized\",\"message\":\"bad key\"}"));

    assertThrows(AuthenticationException.class, () -> enhancedKyc("user-1"));

    assertEquals(1, server.getRequestCount());
  }

  @Test
  void a503OnTheTokenRequestNeverSendsTheJob() {
    smile = TestSupport.clientBuilder(server).maxRetries(0).build();
    server.enqueue(TestSupport.json(503, "{\"status\":\"error\",\"message\":\"busy\"}"));

    assertThrows(ApiException.class, () -> enhancedKyc("user-1"));

    assertEquals(1, server.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"token\":\"\"}", "{\"token\":null}", "{}"})
  void aTokenResponseWithoutATokenNeverSendsTheJob(String body) {
    server.enqueue(TestSupport.json(200, body));

    assertThrows(UnexpectedResponseException.class, () -> enhancedKyc("user-1"));

    assertEquals(1, server.getRequestCount());
  }

  @Test
  void theMintUsesTheRequestTimeout() {
    smile = TestSupport.clientBuilder(server).maxRetries(0).build();
    server.enqueue(TestSupport.tokenResponse("token-1").setBodyDelay(3, TimeUnit.SECONDS));

    assertThrows(
        ConnectionException.class,
        () ->
            smile
                .enhancedKyc()
                .verify(
                    enhancedKycParams("user-1"),
                    RequestOptions.builder().timeout(Duration.ofMillis(300)).build()));

    assertEquals(1, server.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " user-1", "user-1 "})
  void emptyOrPaddedUserIdIsRejectedBeforeAnyRequest(String userId) {
    ValidationException e = assertThrows(ValidationException.class, () -> enhancedKyc(userId));

    assertEquals("user_id must not be empty or padded with whitespace", e.getMessage());
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void aPaddedBodyUserIdIsRejectedBeforeAnyRequest() {
    AuthenticationParams params =
        AuthenticationParams.builder()
            .selfieImage(image())
            .livenessImages(liveness())
            .userDetails(user())
            .consent(consent())
            .userId("user-1 ")
            .build();

    assertThrows(ValidationException.class, () -> smile.biometric().authenticate(params));

    assertEquals(0, server.getRequestCount());
  }

  @Test
  void enrolledImageGrantIsBoundToTheBiometricKycToken() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED));

    enrolledBiometricKyc();

    assertEquals(
        claims("user_id", "user-1", "product", "biometric_kyc", "use_enrolled_image", "true"),
        claims(server.takeRequest()));
    List<TestPart> jobParts = MultipartParser.parse(server.takeRequest());
    assertFalse(MultipartParser.has(jobParts, "use_enrolled_image"));
    assertFalse(MultipartParser.has(jobParts, "selfie_image"));
    assertFalse(MultipartParser.has(jobParts, "liveness_images"));
  }

  @Test
  void grantDoesNotReachLaterRequests() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ACCEPTED));
    enqueueToken();
    server.enqueue(TestSupport.json(200, PROCESSING));

    enrolledBiometricKyc();
    smile.verifications().retrieve("job_x");

    server.takeRequest();
    assertEquals("token-1", server.takeRequest().getHeader("SmileID-Token"));
    RecordedRequest laterToken = server.takeRequest();
    assertEquals("/v3/token", laterToken.getPath());
    assertTrue(claims(laterToken).isEmpty());
    assertEquals("token-2", server.takeRequest().getHeader("SmileID-Token"));
  }

  @Test
  void transportRefusesAGrantWithoutAUser() {
    ApiRequest request =
        BiometricOperations.biometricKycRequest(
            smile.transport(), biometricKycParams().userId(null).build(), RequestOptions.none());

    ValidationException e =
        assertThrows(
            ValidationException.class,
            () -> smile.transport().execute(request, AcceptedResponse.class, true));

    assertEquals("user_id is required when use_enrolled_image is true", e.getMessage());
    assertEquals(0, server.getRequestCount());
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
