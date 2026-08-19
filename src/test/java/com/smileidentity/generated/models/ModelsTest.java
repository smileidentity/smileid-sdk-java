package com.smileidentity.generated.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smileidentity.client.Json;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Spec §5: shared models serialize to verbatim snake_case wire fields. */
class ModelsTest {

  private final ObjectMapper mapper = Json.mapper();

  @Test
  void consentGrantedFactorySerializesToGoldenJson() throws Exception {
    Consent consent =
        Consent.granted(Instant.parse("2026-03-06T12:00:00Z"), "EN", "https://example.com/privacy");
    assertEquals(
        "{\"granted\":true,\"granted_at\":\"2026-03-06T12:00:00.000Z\","
            + "\"notice_language\":\"EN\",\"notice_privacy_policy_url\":\"https://example.com/privacy\"}",
        mapper.writeValueAsString(consent));
  }

  @Test
  void consentGrantedAcceptsPreformattedTimestamp() throws Exception {
    Consent consent =
        Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy");
    assertTrue(
        mapper.writeValueAsString(consent).contains("\"granted_at\":\"2026-03-06T12:00:00.000Z\""));
  }

  @Test
  void userDetailsWithEmailSerializesToGoldenJson() throws Exception {
    UserDetails details =
        UserDetails.builder().givenNames("John").lastName("Doe").email("john@example.com").build();
    assertEquals(
        "{\"given_names\":\"John\",\"last_name\":\"Doe\",\"email\":\"john@example.com\"}",
        mapper.writeValueAsString(details));
  }

  @Test
  void userDetailsWithPhoneSerializesToGoldenJson() throws Exception {
    UserDetails details =
        UserDetails.builder()
            .givenNames("John")
            .lastName("Doe")
            .phoneNumber("+2348012345678")
            .build();
    assertEquals(
        "{\"given_names\":\"John\",\"last_name\":\"Doe\",\"phone_number\":\"+2348012345678\"}",
        mapper.writeValueAsString(details));
  }

  @Test
  void metadataEntrySerializesToNameValueObjects() throws Exception {
    assertEquals(
        "[{\"name\":\"source\",\"value\":\"sdk-test\"}]",
        mapper.writeValueAsString(Arrays.asList(new MetadataEntry("source", "sdk-test"))));
  }

  @Test
  void acceptedResponseNormalizesUppercaseStatus() throws Exception {
    AcceptedResponse r =
        mapper.readValue(
            "{\"status\":\"Accepted\",\"message\":\"Request accepted and queued for processing.\","
                + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\"}",
            AcceptedResponse.class);
    assertTrue(r.isAccepted());
    assertEquals("Accepted", r.getStatus());
    assertEquals("job_01h8x9y2z3a4b5c6d7e8f9g0h1", r.getJobId());
    assertEquals("user_01h8x9y2z3a4b5c6d7e8f9g0h1", r.getUserId());
    assertEquals("Request accepted and queued for processing.", r.getMessage());
    assertNull(r.getCreatedAt());
  }

  @Test
  void acceptedResponseNormalizesLowercaseStatusAndReadsCreatedAt() throws Exception {
    AcceptedResponse r =
        mapper.readValue(
            "{\"status\":\"accepted\",\"message\":\"Request accepted and queued for processing.\","
                + "\"job_id\":\"job_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"created_at\":\"2026-03-10T12:00:00.000Z\"}",
            AcceptedResponse.class);
    assertTrue(r.isAccepted());
    assertEquals("2026-03-10T12:00:00.000Z", r.getCreatedAt());
  }

  @Test
  void acceptedResponseWithOtherStatusIsNotAccepted() throws Exception {
    AcceptedResponse r = mapper.readValue("{\"status\":\"rejected\"}", AcceptedResponse.class);
    assertFalse(r.isAccepted());
  }

  @Test
  void jobStatusParsesTerminalProcessingAndNotFound() throws Exception {
    JobStatus complete =
        mapper.readValue(
            "{\"status\":\"clear\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"message\":\"Job completed\"}",
            JobStatus.class);
    assertTrue(complete.isComplete());
    assertFalse(complete.isProcessing());
    assertEquals("clear", complete.getStatus());
    assertEquals("Job completed", complete.getMessage());

    JobStatus blocked =
        mapper.readValue(
            "{\"status\":\"block\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"message\":\"Job completed\"}",
            JobStatus.class);
    assertTrue(blocked.isComplete());

    JobStatus processing =
        mapper.readValue(
            "{\"status\":\"processing\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\","
                + "\"message\":\"Verification is still processing\"}",
            JobStatus.class);
    assertTrue(processing.isProcessing());

    JobStatus notFound =
        mapper.readValue(
            "{\"status\":\"not_found\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"unknown\",\"message\":\"Verification not found\"}",
            JobStatus.class);
    assertTrue(notFound.isNotFound());
    assertEquals("unknown", notFound.getUserId());
  }

  @Test
  void bankCodesResponseParsesGoldenFixture() throws Exception {
    BankCodesResponse r =
        mapper.readValue(
            "{\"bank_codes\":[{\"code\":\"044\",\"country\":\"NG\",\"name\":\"Access Bank\"},"
                + "{\"code\":\"023\",\"country\":\"NG\",\"name\":\"Citibank\"}]}",
            BankCodesResponse.class);
    assertEquals(2, r.getBankCodes().size());
    assertEquals("044", r.getBankCodes().get(0).getCode());
    assertEquals("NG", r.getBankCodes().get(0).getCountry());
    assertEquals("Access Bank", r.getBankCodes().get(0).getName());
  }

  @Test
  void supportedIdTypesResponseParsesGoldenFixture() throws Exception {
    SupportedIdTypesResponse r =
        mapper.readValue(
            "{\"id_types\":[{\"country\":\"NG\",\"label\":\"Bank Verification Number\","
                + "\"regex\":\"^\\\\d{11}$\",\"required_fields\":[\"first_name\",\"last_name\",\"dob\"],"
                + "\"type\":\"BVN\"},"
                + "{\"bank_code\":\"044\",\"country\":\"NG\",\"label\":\"Bank Account (Access Bank)\","
                + "\"regex\":\"^\\\\d{10}$\",\"required_fields\":[\"first_name\",\"last_name\"],"
                + "\"type\":\"BANK_ACCOUNT\"}]}",
            SupportedIdTypesResponse.class);
    assertEquals(2, r.getIdTypes().size());
    assertNull(r.getIdTypes().get(0).getBankCode());
    assertEquals("BVN", r.getIdTypes().get(0).getType());
    assertEquals(
        Arrays.asList("first_name", "last_name", "dob"), r.getIdTypes().get(0).getRequiredFields());
    assertEquals("044", r.getIdTypes().get(1).getBankCode());
  }

  @Test
  void supportedDocumentsResponseParsesGoldenFixture() throws Exception {
    SupportedDocumentsResponse r =
        mapper.readValue(
            "{\"valid_documents\":[{\"country\":{\"code\":\"NG\",\"name\":\"Nigeria\","
                + "\"continent\":\"AFRICA\"},\"id_types\":[{\"code\":\"DRIVERS_LICENSE\","
                + "\"name\":\"Driver's License\",\"example\":[\"AAA00000AA00\"],\"has_back\":true}]}]}",
            SupportedDocumentsResponse.class);
    assertEquals(1, r.getValidDocuments().size());
    assertEquals("NG", r.getValidDocuments().get(0).getCountry().getCode());
    assertEquals("AFRICA", r.getValidDocuments().get(0).getCountry().getContinent());
    assertEquals("DRIVERS_LICENSE", r.getValidDocuments().get(0).getIdTypes().get(0).getCode());
    assertTrue(r.getValidDocuments().get(0).getIdTypes().get(0).getHasBack());
    assertEquals(
        Arrays.asList("AAA00000AA00"),
        r.getValidDocuments().get(0).getIdTypes().get(0).getExample());
  }

  @Test
  void idStatusResponseParsesGoldenFixture() throws Exception {
    IdStatusResponse r =
        mapper.readValue(
            "{\"last_checked\":\"2026-04-14T12:30:00.000Z\",\"last_check_status\":\"success\","
                + "\"last_hour_success_rate\":\"95%\",\"last_known_status\":\"online\","
                + "\"last_check_success_rate\":\"90%\"}",
            IdStatusResponse.class);
    assertEquals("2026-04-14T12:30:00.000Z", r.getLastChecked());
    assertEquals("success", r.getLastCheckStatus());
    assertEquals("95%", r.getLastHourSuccessRate());
    assertEquals("online", r.getLastKnownStatus());
    assertEquals("90%", r.getLastCheckSuccessRate());
  }

  @Test
  void replayCallbackResponseParsesGoldenFixture() throws Exception {
    ReplayCallbackResponse r =
        mapper.readValue(
            "{\"status\":\"accepted\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"test-user\",\"message\":\"Callback replay queued successfully.\"}",
            ReplayCallbackResponse.class);
    assertEquals("accepted", r.getStatus());
    assertEquals("job_01h2xcejqtf2nbrexx3vqjhp41", r.getJobId());
    assertEquals("test-user", r.getUserId());
    assertEquals("Callback replay queued successfully.", r.getMessage());
  }

  @Test
  void reportUserFraudResponseParsesGoldenFixture() throws Exception {
    ReportUserFraudResponse r =
        mapper.readValue(
            "{\"status\":\"accepted\",\"message\":\"Fraud report accepted\",\"user_id\":\"user-123\"}",
            ReportUserFraudResponse.class);
    assertEquals("accepted", r.getStatus());
    assertEquals("Fraud report accepted", r.getMessage());
    assertEquals("user-123", r.getUserId());
  }

  @Test
  void responseParsingIgnoresUnknownFields() throws Exception {
    AcceptedResponse r =
        mapper.readValue(
            "{\"status\":\"Accepted\",\"brand_new_field\":42}", AcceptedResponse.class);
    assertTrue(r.isAccepted());
  }
}
