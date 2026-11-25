package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.errors.ConflictException;
import com.smileidentity.errors.NotFoundException;
import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AddressAcceptedResponse;
import com.smileidentity.generated.models.AddressVerificationParams;
import com.smileidentity.generated.models.BasicKycParams;
import com.smileidentity.generated.models.BasicKycResult;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.OneTimeAmlCallback;
import com.smileidentity.generated.models.OneTimeAmlParams;
import com.smileidentity.generated.models.PhoneVerificationParams;
import com.smileidentity.generated.models.SmileLinkCreateParams;
import com.smileidentity.generated.models.SmileLinkCreated;
import com.smileidentity.generated.models.SmileLinkIdType;
import com.smileidentity.generated.models.SmileLinkListItem;
import com.smileidentity.generated.models.SmileLinkListParams;
import com.smileidentity.generated.models.SmileLinkListResponse;
import com.smileidentity.generated.models.SmileLinkResource;
import com.smileidentity.generated.models.SmileLinkUpdateParams;
import com.smileidentity.generated.models.UserDetails;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Golden fixtures for the operations and response headers added by the spec backfill: address and
 * phone verification, the Smile Link operations, and the User-ID, Response-Signature and
 * Response-Timestamp headers of the Basic KYC and One-Time AML sync results.
 */
class BackfillGoldenTest {

  private static final String CONSENT_JSON =
      "{\"granted\":true,\"granted_at\":\"2026-03-06T12:00:00.000Z\",\"notice_language\":\"EN\","
          + "\"notice_privacy_policy_url\":\"https://example.com/privacy\"}";
  private static final String USER_ID = "user_01h8x9y2z3a4b5c6d7e8f9g0h1";
  private static final String JOB_ID = "job_01h8x9y2z3a4b5c6d7e8f9g0h1";
  private static final String REF_ID = "65983864-e8c6-4d09-890a-2e5f88fc50f6";
  private static final String ADDRESS_ACCEPTED =
      "{\"status\":\"accepted\",\"message\":\"Job submitted successfully\","
          + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\","
          + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\"}";
  private static final String PHONE_ACCEPTED =
      "{\"status\":\"accepted\",\"message\":\"Job submitted successfully\","
          + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\","
          + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
          + "\"created_at\":\"2026-03-10T12:00:00.000Z\"}";
  private static final String BASIC_KYC_BODY =
      "{\"status\":\"clear\",\"message\":\"Verification successful: exact match.\","
          + "\"reason\":null,\"created_at\":\"2026-03-10T12:00:00.000Z\",\"product\":\"basic_kyc\"}";
  private static final String AML_BODY =
      "{\"status\":\"clear\",\"message\":\"Screening completed: no matches found.\","
          + "\"reason\":null,\"created_at\":\"2026-07-20T12:00:00.000Z\",\"product\":\"one_time_aml\","
          + "\"people\":[]}";
  private static final String LINK_JSON =
      "{\"ref_id\":\"65983864-e8c6-4d09-890a-2e5f88fc50f6\","
          + "\"link\":\"https://links.usesmileid.com/v3/2/65983864-e8c6-4d09-890a-2e5f88fc50f6\","
          + "\"name\":\"Onboarding\",\"company_name\":\"Acme Pay\",\"description\":null,"
          + "\"id_types\":[{\"country\":\"NG\",\"id_type\":\"NIN\",\"verification_method\":\"enhanced_kyc\"}],"
          + "\"is_single_use\":true,\"callback_url\":\"https://example.com/callback\","
          + "\"notice_privacy_policy_url\":\"https://partner.example/privacy\","
          + "\"logo_url\":\"https://partner.example/logo.png\",\"redirect_url\":null,"
          + "\"theme_color\":\"#000\",\"expires_at\":\"2026-06-08T12:00:00.000Z\","
          + "\"disabled_at\":\"2026-03-11T12:00:00.000Z\",\"partner_params\":{\"order_id\":\"ORD-1\"},"
          + "\"user_id\":\"user-123\",\"token_provided_fields\":[\"country\",\"id_type\"],"
          + "\"token_provided_values\":{\"country\":\"NG\",\"id_type\":\"NIN\"},"
          + "\"created_at\":\"2026-03-10T12:00:00.000Z\",\"updated_at\":\"2026-03-10T12:30:00.000Z\"";

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

  private static Consent consent() {
    return Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy");
  }

  private static UserDetails amina() {
    return UserDetails.builder()
        .givenNames("Amina")
        .lastName("Clearwater")
        .email("amina@example.com")
        .build();
  }

  // ------------------------------------------------------------ response headers

  private static BasicKycParams.Builder basicKyc() {
    return BasicKycParams.builder()
        .country("NG")
        .idType("NIN")
        .idNumber("12345678901")
        .userDetails(amina())
        .consent(consent());
  }

  private static OneTimeAmlParams.Builder oneTimeAml() {
    return OneTimeAmlParams.builder()
        .countries(Arrays.asList("NG"))
        .fullName("Gbenga Afolabi")
        .userDetails(amina())
        .consent(consent());
  }

  @Test
  void basicKycSyncExposesTheUserIdAndSignatureHeaders() throws Exception {
    enqueueToken();
    // Header names arrive lower-cased through some proxies; the lookup ignores case.
    server.enqueue(
        TestSupport.json(200, BASIC_KYC_BODY)
            .setHeader("user-id", USER_ID)
            .setHeader("response-signature", "c2lnbmF0dXJl")
            .setHeader("response-timestamp", "2026-03-10T12:00:04.000Z"));

    BasicKycResult result = smile.basicKyc().verifySync(basicKyc().build());

    assertEquals(USER_ID, result.getUserId());
    assertEquals("c2lnbmF0dXJl", result.getResponseSignature());
    assertEquals("2026-03-10T12:00:04.000Z", result.getResponseTimestamp());
    assertEquals("clear", result.getStatus());
  }

  @Test
  void basicKycSyncHasNoUserIdOrSignatureWithoutTheHeaders() throws Exception {
    enqueueToken();
    // Fields in the body are not headers; a blank header counts as absent.
    server.enqueue(
        TestSupport.json(
                200,
                BASIC_KYC_BODY.replace(
                    "}",
                    ",\"response_signature\":\"from_body\",\"response_timestamp\":\"from_body\"}"))
            .setHeader("User-ID", " "));

    BasicKycResult result = smile.basicKyc().verifySync(basicKyc().build());

    assertNull(result.getUserId());
    assertNull(result.getResponseSignature());
    assertNull(result.getResponseTimestamp());
  }

  @Test
  void oneTimeAmlSyncExposesTheUserIdAndSignatureHeaders() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(200, AML_BODY)
            .setHeader("SmileID-Job-ID", JOB_ID)
            .setHeader("User-ID", USER_ID)
            .setHeader("Response-Signature", "Zm9vYmFy")
            .setHeader("Response-Timestamp", "2026-07-20T12:00:01.000Z"));

    OneTimeAmlCallback result = smile.aml().oneTimeSync(oneTimeAml().build());

    assertEquals(JOB_ID, result.getJobId());
    assertEquals(USER_ID, result.getUserId());
    assertEquals("Zm9vYmFy", result.getResponseSignature());
    assertEquals("2026-07-20T12:00:01.000Z", result.getResponseTimestamp());
  }

  @Test
  void oneTimeAmlSyncHasNoUserIdOrSignatureWithoutTheHeaders() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, AML_BODY));

    OneTimeAmlCallback result = smile.aml().oneTimeSync(oneTimeAml().build());

    assertNull(result.getUserId());
    assertNull(result.getResponseSignature());
    assertNull(result.getResponseTimestamp());
  }

  // ------------------------------------------------------------------ address

  private static AddressVerificationParams.Builder nigeriaAddress() {
    return AddressVerificationParams.builder()
        .address("12 Test Road, Testville")
        .country("NG")
        .utilityNumber("0123456789")
        .utilityProvider("IKEDC")
        .utilityType("PrePaid")
        .userDetails(amina())
        .consent(consent())
        .callbackUrl("https://example.com/callback");
  }

  @Test
  void addressVerificationMatchesTheGoldenRequest() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, ADDRESS_ACCEPTED));

    AddressAcceptedResponse accepted =
        smile
            .address()
            .verify(
                nigeriaAddress()
                    .userId(USER_ID)
                    .idNumber("9001015800085")
                    .partnerParams(Collections.singletonMap("order_id", "ORD-1"))
                    .metadata(Arrays.asList(new MetadataEntry("k", "v")))
                    .build());

    List<TestPart> claims = MultipartParser.parse(server.takeRequest());
    assertEquals("address_verification", MultipartParser.single(claims, "product").body);
    assertEquals(USER_ID, MultipartParser.single(claims, "user_id").body);
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/address", r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertNull(r.getHeader("SmileID-Partner-ID"));
    assertNull(r.getHeader("User-ID"), "the user id is a body part on this endpoint");
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals(12, parts.size());
    assertEquals("12 Test Road, Testville", MultipartParser.single(parts, "address").body);
    assertEquals("NG", MultipartParser.single(parts, "country").body);
    assertEquals("9001015800085", MultipartParser.single(parts, "id_number").body);
    assertEquals("0123456789", MultipartParser.single(parts, "utility_number").body);
    assertEquals("IKEDC", MultipartParser.single(parts, "utility_provider").body);
    assertEquals("PrePaid", MultipartParser.single(parts, "utility_type").body);
    TestPart userDetails = MultipartParser.single(parts, "user_details");
    assertEquals("application/json", userDetails.contentType.split(";")[0]);
    assertEquals(
        "{\"given_names\":\"Amina\",\"last_name\":\"Clearwater\",\"email\":\"amina@example.com\"}",
        userDetails.body);
    assertEquals(CONSENT_JSON, MultipartParser.single(parts, "consent").body);
    assertEquals(USER_ID, MultipartParser.single(parts, "user_id").body);
    assertEquals(
        "https://example.com/callback", MultipartParser.single(parts, "callback_url").body);
    assertEquals("{\"order_id\":\"ORD-1\"}", MultipartParser.single(parts, "partner_params").body);
    assertEquals(
        "[{\"name\":\"k\",\"value\":\"v\"}]", MultipartParser.single(parts, "metadata").body);
    assertNull(MultipartParser.single(parts, "address").contentType);

    assertEquals("accepted", accepted.getStatus());
    assertEquals("Job submitted successfully", accepted.getMessage());
    assertEquals(JOB_ID, accepted.getJobId());
    assertEquals(USER_ID, accepted.getUserId());
  }

  @Test
  void addressVerificationOmitsAbsentOptionalsAndUsesTheDefaultCallbackUrl() throws Exception {
    SmileID withDefault =
        TestSupport.clientBuilder(server).defaultCallbackUrl("https://example.com/default").build();
    enqueueToken();
    server.enqueue(TestSupport.json(202, ADDRESS_ACCEPTED));

    withDefault
        .address()
        .verify(
            AddressVerificationParams.builder()
                .address("1 Rue")
                .country("ZA")
                .idNumber("9001015800085")
                .userDetails(amina())
                .consent(consent())
                .build());

    List<TestPart> claims = MultipartParser.parse(server.takeRequest());
    assertEquals("address_verification", MultipartParser.single(claims, "product").body);
    assertFalse(MultipartParser.has(claims, "user_id"));
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals(6, parts.size());
    assertEquals("https://example.com/default", MultipartParser.single(parts, "callback_url").body);
    assertFalse(MultipartParser.has(parts, "utility_number"));
    assertFalse(MultipartParser.has(parts, "user_id"));
  }

  @Test
  void addressVerificationRejectsInvalidParamsBeforeSending() {
    assertRejects(() -> smile.address().verify(nigeriaAddress().address(null).build()));
    assertRejects(() -> smile.address().verify(nigeriaAddress().address(" ").build()));
    assertRejects(() -> smile.address().verify(nigeriaAddress().country(null).build()));
    assertRejects(() -> smile.address().verify(nigeriaAddress().utilityNumber(null).build()));
    assertRejects(
        () -> smile.address().verify(nigeriaAddress().utilityNumber(null).idNumber(" ").build()));
    assertRejects(() -> smile.address().verify(nigeriaAddress().userDetails(null).build()));
    assertRejects(() -> smile.address().verify(nigeriaAddress().consent(null).build()));
    assertRejects(
        () -> smile.address().verify(nigeriaAddress().callbackUrl("http://example.com").build()));
    assertEquals(0, server.getRequestCount());
  }

  private static void assertRejects(org.junit.jupiter.api.function.Executable call) {
    assertThrows(ValidationException.class, call);
  }

  // -------------------------------------------------------------------- phone

  private static PhoneVerificationParams.Builder ghanaPhone() {
    return PhoneVerificationParams.builder()
        .country("GH")
        .phoneNumber("+233241234567")
        .operator("MTN")
        .userDetails(amina())
        .consent(consent())
        .callbackUrl("https://example.com/callback");
  }

  @Test
  void phoneVerificationMatchesTheGoldenRequest() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, PHONE_ACCEPTED));

    AcceptedResponse accepted =
        smile
            .phone()
            .verify(
                ghanaPhone()
                    .idNumber("12345678901")
                    .userId(USER_ID)
                    .partnerParams(Collections.singletonMap("order_id", "ORD-1"))
                    .metadata(Arrays.asList(new MetadataEntry("k", "v")))
                    .build());

    List<TestPart> claims = MultipartParser.parse(server.takeRequest());
    assertEquals("phone_number_verification", MultipartParser.single(claims, "product").body);
    assertEquals(USER_ID, MultipartParser.single(claims, "user_id").body);
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/phone", r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertNull(r.getHeader("SmileID-Partner-ID"));
    assertNull(r.getHeader("User-ID"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals(10, parts.size());
    assertEquals("GH", MultipartParser.single(parts, "country").body);
    assertEquals("+233241234567", MultipartParser.single(parts, "phone_number").body);
    assertEquals("MTN", MultipartParser.single(parts, "operator").body);
    assertEquals("12345678901", MultipartParser.single(parts, "id_number").body);
    assertEquals(
        "{\"given_names\":\"Amina\",\"last_name\":\"Clearwater\",\"email\":\"amina@example.com\"}",
        MultipartParser.single(parts, "user_details").body);
    assertEquals(CONSENT_JSON, MultipartParser.single(parts, "consent").body);
    assertEquals(USER_ID, MultipartParser.single(parts, "user_id").body);
    assertEquals(
        "https://example.com/callback", MultipartParser.single(parts, "callback_url").body);
    assertEquals("{\"order_id\":\"ORD-1\"}", MultipartParser.single(parts, "partner_params").body);
    assertEquals(
        "[{\"name\":\"k\",\"value\":\"v\"}]", MultipartParser.single(parts, "metadata").body);

    assertTrue(accepted.isAccepted());
    assertEquals(JOB_ID, accepted.getJobId());
    assertEquals(USER_ID, accepted.getUserId());
    assertEquals("2026-03-10T12:00:00.000Z", accepted.getCreatedAt());
  }

  @Test
  void phoneVerificationOmitsAbsentOptionals() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, PHONE_ACCEPTED));

    smile.phone().verify(ghanaPhone().operator(null).callbackUrl(null).build());

    MultipartParser.parse(server.takeRequest());
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals(4, parts.size());
    assertFalse(MultipartParser.has(parts, "operator"));
    assertFalse(MultipartParser.has(parts, "callback_url"));
    assertFalse(MultipartParser.has(parts, "id_number"));
  }

  @Test
  void phoneVerificationRejectsInvalidParamsBeforeSending() {
    assertRejects(() -> smile.phone().verify(ghanaPhone().country(null).build()));
    assertRejects(() -> smile.phone().verify(ghanaPhone().phoneNumber(" ").build()));
    assertRejects(() -> smile.phone().verify(ghanaPhone().userDetails(null).build()));
    assertRejects(() -> smile.phone().verify(ghanaPhone().consent(null).build()));
    assertRejects(() -> smile.phone().verify(ghanaPhone().callbackUrl("http://x.example").build()));
    assertEquals(0, server.getRequestCount());
  }

  // -------------------------------------------------------------- smile links

  private static List<SmileLinkIdType> nin() {
    return Arrays.asList(new SmileLinkIdType("NG", "NIN", "enhanced_kyc"));
  }

  private static SmileLinkCreateParams.Builder singleUseLink() {
    return SmileLinkCreateParams.builder()
        .idTypes(nin())
        .name("KYC for Ada")
        .noticePrivacyPolicyUrl("https://partner.example/privacy")
        .isSingleUse(true);
  }

  /** The token request of a smile_links call carries no claims at all. */
  private void assertTokenHasNoClaims() throws Exception {
    RecordedRequest token = server.takeRequest();
    assertEquals("POST", token.getMethod());
    assertEquals("/v3/token", token.getPath());
    assertEquals(0, token.getBodySize(), "no product or user_id claim");
  }

  @Test
  void createSmileLinkMatchesTheGoldenRequest() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            201,
            "{\"link\":\"https://links.usesmileid.com/v3/2/"
                + REF_ID
                + "\","
                + "\"message\":\"Smile Link created\",\"ref_id\":\""
                + REF_ID
                + "\","
                + "\"user_id\":\"user-123\"}"));

    SmileLinkCreated created =
        smile
            .smileLinks()
            .create(
                singleUseLink()
                    .companyName("Acme Pay")
                    .callbackUrl("https://example.com/callback")
                    .description("Onboarding")
                    .expiresAt("2026-06-08T12:00:00.000Z")
                    .logoUrl("https://partner.example/logo.png")
                    .redirectUrl("https://partner.example/done")
                    .themeColor("#0a0a0a")
                    .partnerParams(Collections.singletonMap("order_id", "ORD-1"))
                    .userId("user-123")
                    .build());

    assertTokenHasNoClaims();
    RecordedRequest r = server.takeRequest();
    assertEquals("POST", r.getMethod());
    assertEquals("/v3/smile_links", r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertNull(r.getHeader("SmileID-Partner-ID"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals(13, parts.size());
    TestPart idTypes = MultipartParser.single(parts, "id_types");
    assertEquals("application/json", idTypes.contentType.split(";")[0]);
    assertEquals(
        "[{\"country\":\"NG\",\"id_type\":\"NIN\",\"verification_method\":\"enhanced_kyc\"}]",
        idTypes.body);
    assertEquals("KYC for Ada", MultipartParser.single(parts, "name").body);
    assertEquals("Acme Pay", MultipartParser.single(parts, "company_name").body);
    assertEquals(
        "https://partner.example/privacy",
        MultipartParser.single(parts, "notice_privacy_policy_url").body);
    assertEquals("true", MultipartParser.single(parts, "is_single_use").body);
    assertEquals(
        "https://example.com/callback", MultipartParser.single(parts, "callback_url").body);
    assertEquals("Onboarding", MultipartParser.single(parts, "description").body);
    assertEquals("2026-06-08T12:00:00.000Z", MultipartParser.single(parts, "expires_at").body);
    assertEquals(
        "https://partner.example/logo.png", MultipartParser.single(parts, "logo_url").body);
    assertEquals(
        "https://partner.example/done", MultipartParser.single(parts, "redirect_url").body);
    assertEquals("#0a0a0a", MultipartParser.single(parts, "theme_color").body);
    assertEquals("{\"order_id\":\"ORD-1\"}", MultipartParser.single(parts, "partner_params").body);
    assertEquals("user-123", MultipartParser.single(parts, "user_id").body);

    assertEquals("https://links.usesmileid.com/v3/2/" + REF_ID, created.getLink());
    assertEquals("Smile Link created", created.getMessage());
    assertEquals(REF_ID, created.getRefId());
    assertEquals("user-123", created.getUserId());
  }

  @Test
  void createSmileLinkSendsOnlyTheRequiredPartsAndTheDefaultCallbackUrl() throws Exception {
    SmileID withDefault =
        TestSupport.clientBuilder(server).defaultCallbackUrl("https://example.com/default").build();
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            201,
            "{\"link\":\"https://l.example/x\",\"message\":\"Smile Link created\","
                + "\"ref_id\":\""
                + REF_ID
                + "\",\"user_id\":null}"));

    SmileLinkCreated created =
        withDefault.smileLinks().create(singleUseLink().isSingleUse(false).build());

    assertTokenHasNoClaims();
    List<TestPart> parts = MultipartParser.parse(server.takeRequest());
    assertEquals(5, parts.size());
    assertEquals("false", MultipartParser.single(parts, "is_single_use").body);
    assertEquals("https://example.com/default", MultipartParser.single(parts, "callback_url").body);
    assertNull(created.getUserId(), "a reusable link has no user id");
  }

  @Test
  void createSmileLinkUserIdTakenIsAConflict() {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            409,
            "{\"message\":\"User ID is taken. Please provide a unique ID.\",\"status\":\"Conflict\"}"));

    ConflictException e =
        assertThrows(
            ConflictException.class, () -> smile.smileLinks().create(singleUseLink().build()));
    assertEquals("User ID is taken. Please provide a unique ID.", e.getMessage());
    assertEquals(2, server.getRequestCount(), "a conflict is never retried");
  }

  @Test
  void createSmileLinkRejectsInvalidParamsBeforeSending() {
    SmileLinkCreateParams missingIdTypes = singleUseLink().idTypes(null).build();
    assertRejects(() -> smile.smileLinks().create(missingIdTypes));
    assertRejects(
        () -> smile.smileLinks().create(singleUseLink().idTypes(new ArrayList<>()).build()));
    assertRejects(() -> smile.smileLinks().create(singleUseLink().name(null).build()));
    assertRejects(
        () -> smile.smileLinks().create(singleUseLink().noticePrivacyPolicyUrl(" ").build()));
    assertRejects(() -> smile.smileLinks().create(singleUseLink().isSingleUse(null).build()));
    assertRejects(
        () -> smile.smileLinks().create(singleUseLink().callbackUrl("http://x.example").build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void getSmileLinkParsesTheLinkResource() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));

    SmileLinkResource link = smile.smileLinks().get(REF_ID);

    assertTokenHasNoClaims();
    RecordedRequest r = server.takeRequest();
    assertEquals("GET", r.getMethod());
    assertEquals("/v3/smile_links/" + REF_ID, r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertResource(link);
  }

  private static void assertResource(SmileLinkResource link) {
    assertEquals(REF_ID, link.getRefId());
    assertEquals("https://links.usesmileid.com/v3/2/" + REF_ID, link.getLink());
    assertEquals("Onboarding", link.getName());
    assertEquals("Acme Pay", link.getCompanyName());
    assertNull(link.getDescription());
    assertEquals(1, link.getIdTypes().size());
    assertEquals("NG", link.getIdTypes().get(0).getCountry());
    assertEquals("NIN", link.getIdTypes().get(0).getIdType());
    assertEquals("enhanced_kyc", link.getIdTypes().get(0).getVerificationMethod());
    assertEquals(Boolean.TRUE, link.getIsSingleUse());
    assertEquals("https://example.com/callback", link.getCallbackUrl());
    assertEquals("https://partner.example/privacy", link.getNoticePrivacyPolicyUrl());
    assertEquals("https://partner.example/logo.png", link.getLogoUrl());
    assertNull(link.getRedirectUrl());
    assertEquals("#000", link.getThemeColor());
    assertEquals("2026-06-08T12:00:00.000Z", link.getExpiresAt());
    assertEquals("2026-03-11T12:00:00.000Z", link.getDisabledAt());
    assertEquals("ORD-1", link.getPartnerParams().get("order_id"));
    assertEquals("user-123", link.getUserId());
    assertEquals(Arrays.asList("country", "id_type"), link.getTokenProvidedFields());
    assertEquals("NG", link.getTokenProvidedValues().get("country"));
    assertEquals("2026-03-10T12:00:00.000Z", link.getCreatedAt());
    assertEquals("2026-03-10T12:30:00.000Z", link.getUpdatedAt());
  }

  @Test
  void getSmileLinkEncodesTheRefIdAsOnePathSegment() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));

    smile.smileLinks().get("a/b c");

    server.takeRequest();
    assertEquals("/v3/smile_links/a%2Fb%20c", server.takeRequest().getPath());
  }

  @Test
  void getSmileLinkIsRetriedOnA503() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(503, "{\"status\":\"Service Unavailable\"}"));
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));

    smile.smileLinks().get(REF_ID);

    assertEquals(3, server.getRequestCount());
  }

  @Test
  void getSmileLinkNotFoundRaises() {
    enqueueToken();
    server.enqueue(
        TestSupport.json(404, "{\"message\":\"Link not found\",\"status\":\"Not Found\"}"));

    NotFoundException e =
        assertThrows(NotFoundException.class, () -> smile.smileLinks().get(REF_ID));
    assertEquals("Link not found", e.getMessage());
  }

  @Test
  void getSmileLinkRequiresARefId() {
    assertRejects(() -> smile.smileLinks().get(null));
    assertRejects(() -> smile.smileLinks().get(" "));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void updateSmileLinkSendsIdTypesAndIsDisabled() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));

    SmileLinkResource link =
        smile
            .smileLinks()
            .update(
                REF_ID, SmileLinkUpdateParams.builder().idTypes(nin()).isDisabled(true).build());

    assertTokenHasNoClaims();
    RecordedRequest r = server.takeRequest();
    assertEquals("PUT", r.getMethod());
    assertEquals("/v3/smile_links/" + REF_ID, r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    List<TestPart> parts = MultipartParser.parse(r);
    assertEquals(2, parts.size());
    assertEquals(
        "[{\"country\":\"NG\",\"id_type\":\"NIN\",\"verification_method\":\"enhanced_kyc\"}]",
        MultipartParser.single(parts, "id_types").body);
    assertEquals("true", MultipartParser.single(parts, "is_disabled").body);
    assertResource(link);
  }

  @Test
  void updateSmileLinkSendsEitherFieldAlone() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));
    enqueueToken();
    server.enqueue(TestSupport.json(200, LINK_JSON + "}"));

    smile.smileLinks().update(REF_ID, SmileLinkUpdateParams.builder().isDisabled(false).build());
    smile.smileLinks().update(REF_ID, SmileLinkUpdateParams.builder().idTypes(nin()).build());

    server.takeRequest();
    List<TestPart> disable = MultipartParser.parse(server.takeRequest());
    assertEquals(1, disable.size());
    assertEquals("false", MultipartParser.single(disable, "is_disabled").body);
    server.takeRequest();
    List<TestPart> replace = MultipartParser.parse(server.takeRequest());
    assertEquals(1, replace.size());
    assertTrue(MultipartParser.has(replace, "id_types"));
  }

  @Test
  void updateSmileLinkIsNeverRetried() {
    enqueueToken();
    server.enqueue(TestSupport.json(503, "{\"status\":\"Service Unavailable\"}"));

    assertThrows(
        RuntimeException.class,
        () ->
            smile
                .smileLinks()
                .update(REF_ID, SmileLinkUpdateParams.builder().isDisabled(true).build()));
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void updateSmileLinkRejectsInvalidParamsBeforeSending() {
    SmileLinkUpdateParams empty = SmileLinkUpdateParams.builder().build();
    assertRejects(() -> smile.smileLinks().update(null, empty));
    assertRejects(() -> smile.smileLinks().update(REF_ID, empty));
    assertRejects(
        () ->
            smile
                .smileLinks()
                .update(
                    REF_ID, SmileLinkUpdateParams.builder().idTypes(new ArrayList<>()).build()));
    assertEquals(0, server.getRequestCount());
  }

  @Test
  void listSmileLinksParsesLinksWithJobStats() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            200,
            "{\"links\":["
                + LINK_JSON
                + ",\"clear\":3,\"block\":1,\"attention\":2,\"error\":1,\"successful_jobs\":5,"
                + "\"failed_jobs\":2,\"total_jobs\":7}],"
                + "\"meta\":{\"current_page\":2,\"limit\":20,\"total_count\":42,\"total_pages\":3}}"));

    SmileLinkListResponse page =
        smile
            .smileLinks()
            .list(SmileLinkListParams.builder().limit(20).page(2).singleUse(true).build());

    assertTokenHasNoClaims();
    RecordedRequest r = server.takeRequest();
    assertEquals("GET", r.getMethod());
    assertEquals("/v3/smile_links?limit=20&page=2&single_use=true", r.getPath());
    assertNotNull(r.getHeader("SmileID-Token"));
    assertEquals(1, page.getLinks().size());
    SmileLinkListItem item = page.getLinks().get(0);
    assertResource(item);
    assertEquals(Integer.valueOf(3), item.getClear());
    assertEquals(Integer.valueOf(1), item.getBlock());
    assertEquals(Integer.valueOf(2), item.getAttention());
    assertEquals(Integer.valueOf(1), item.getError());
    assertEquals(Integer.valueOf(5), item.getSuccessfulJobs());
    assertEquals(Integer.valueOf(2), item.getFailedJobs());
    assertEquals(Integer.valueOf(7), item.getTotalJobs());
    assertEquals(Integer.valueOf(2), page.getMeta().getCurrentPage());
    assertEquals(Integer.valueOf(20), page.getMeta().getLimit());
    assertEquals(Integer.valueOf(42), page.getMeta().getTotalCount());
    assertEquals(Integer.valueOf(3), page.getMeta().getTotalPages());
  }

  @Test
  void listSmileLinksSendsNoQueryByDefault() throws Exception {
    String empty =
        "{\"links\":[],\"meta\":{\"current_page\":1,\"limit\":20,\"total_count\":0,\"total_pages\":0}}";
    for (int i = 0; i < 3; i++) {
      enqueueToken();
      server.enqueue(TestSupport.json(200, empty));
    }

    assertTrue(smile.smileLinks().list().getLinks().isEmpty());
    smile.smileLinks().list(SmileLinkListParams.builder().build());
    smile.smileLinks().list(SmileLinkListParams.builder().singleUse(false).build());

    server.takeRequest();
    assertEquals("/v3/smile_links", server.takeRequest().getPath());
    server.takeRequest();
    assertEquals("/v3/smile_links", server.takeRequest().getPath());
    server.takeRequest();
    assertEquals("/v3/smile_links?single_use=false", server.takeRequest().getPath());
  }

  @Test
  void listSmileLinksIsRetriedOnA503() {
    enqueueToken();
    server.enqueue(TestSupport.json(503, "{\"status\":\"Service Unavailable\"}"));
    server.enqueue(
        TestSupport.json(
            200,
            "{\"links\":[],\"meta\":{\"current_page\":1,\"limit\":20,\"total_count\":0,"
                + "\"total_pages\":0}}"));

    assertTrue(smile.smileLinks().list().getLinks().isEmpty());
    assertEquals(3, server.getRequestCount());
  }

  @Test
  void createSmileLinkIsNeverRetried() {
    enqueueToken();
    server.enqueue(TestSupport.json(503, "{\"status\":\"Service Unavailable\"}"));

    assertThrows(RuntimeException.class, () -> smile.smileLinks().create(singleUseLink().build()));
    assertEquals(2, server.getRequestCount());
  }
}
