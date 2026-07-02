package com.smileidentity.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Spec §7: typed error hierarchy over both wire error shapes. */
class ErrorParserTest {

  @Test
  void parsesStatusMessageShapeInto400InvalidRequest() {
    String body =
        "{\"status\":\"Bad Request\",\"message\":\"Either email or phone_number is required.\"}";
    SmileIDException e = ErrorParser.parse(400, body, null);
    assertInstanceOf(InvalidRequestException.class, e);
    assertEquals(400, e.getStatusCode());
    assertEquals("Bad Request", e.getStatus());
    assertEquals("Either email or phone_number is required.", e.getMessage());
    assertNull(e.getCode());
    assertEquals(body, e.getRawBody());
  }

  @Test
  void parsesErrorCodeShapeInto403Permission() {
    String body = "{\"error\":\"You are not authorized to do that.\",\"code\":\"2413\"}";
    SmileIDException e = ErrorParser.parse(403, body, null);
    assertInstanceOf(PermissionException.class, e);
    assertEquals(403, e.getStatusCode());
    assertEquals("You are not authorized to do that.", e.getMessage());
    assertEquals("2413", e.getCode());
    assertNull(e.getStatus());
  }

  @Test
  void parsesReorderedMessageStatusShape() {
    String body = "{\"message\":\"\\\"country\\\" is required\",\"status\":\"Bad Request\"}";
    SmileIDException e = ErrorParser.parse(400, body, null);
    assertInstanceOf(InvalidRequestException.class, e);
    assertEquals("\"country\" is required", e.getMessage());
    assertEquals("Bad Request", e.getStatus());
  }

  @Test
  void mapsEachHttpStatusToItsClass() {
    assertInstanceOf(InvalidRequestException.class, ErrorParser.parse(400, "{}", null));
    assertInstanceOf(AuthenticationException.class, ErrorParser.parse(401, "{}", null));
    assertInstanceOf(PaymentRequiredException.class, ErrorParser.parse(402, "{}", null));
    assertInstanceOf(PermissionException.class, ErrorParser.parse(403, "{}", null));
    assertInstanceOf(NotFoundException.class, ErrorParser.parse(404, "{}", null));
    assertInstanceOf(ConflictException.class, ErrorParser.parse(409, "{}", null));
    assertInstanceOf(PayloadTooLargeException.class, ErrorParser.parse(413, "{}", null));
    assertInstanceOf(InvalidRequestException.class, ErrorParser.parse(415, "{}", null));
    assertInstanceOf(RateLimitException.class, ErrorParser.parse(429, "{}", null));
    assertInstanceOf(ApiException.class, ErrorParser.parse(500, "{}", null));
    assertInstanceOf(ApiException.class, ErrorParser.parse(502, "{}", null));
    assertInstanceOf(ApiException.class, ErrorParser.parse(503, "{}", null));
    assertInstanceOf(ApiException.class, ErrorParser.parse(504, "{}", null));
  }

  @Test
  void paymentRequiredGoldenFixture() {
    String body = "{\"status\":\"Payment Required\",\"message\":\"Insufficient wallet balance.\"}";
    SmileIDException e = ErrorParser.parse(402, body, null);
    assertInstanceOf(PaymentRequiredException.class, e);
    assertEquals("Insufficient wallet balance.", e.getMessage());
  }

  @Test
  void conflictGoldenFixtureForReplay() {
    String body =
        "{\"status\":\"Conflict\",\"message\":\"Verification is still processing. "
            + "Callbacks can only be replayed for completed verifications.\"}";
    SmileIDException e = ErrorParser.parse(409, body, null);
    assertInstanceOf(ConflictException.class, e);
    assertEquals("Conflict", e.getStatus());
  }

  @Test
  void payloadTooLargeGoldenFixture() {
    String body = "{\"status\":\"Content Too Large\",\"message\":\"selfie_image is too large.\"}";
    SmileIDException e = ErrorParser.parse(413, body, null);
    assertInstanceOf(PayloadTooLargeException.class, e);
    assertEquals("selfie_image is too large.", e.getMessage());
  }

  @Test
  void nonJsonBodyFallsBackToHttpReasonAndKeepsRawBody() {
    SmileIDException e = ErrorParser.parse(500, "<html>oops</html>", null);
    assertInstanceOf(ApiException.class, e);
    assertEquals("<html>oops</html>", e.getRawBody());
    assertTrue(e.getMessage() != null && !e.getMessage().isEmpty());
  }

  @Test
  void allErrorsShareTheBaseType() {
    assertInstanceOf(SmileIDException.class, ErrorParser.parse(400, "{}", null));
    assertInstanceOf(SmileIDException.class, new ConnectionException("boom", null));
    assertInstanceOf(SmileIDException.class, new TimeoutException("too slow"));
    assertInstanceOf(SmileIDException.class, new ValidationException("bad input"));
    assertInstanceOf(InvalidRequestException.class, new ValidationException("bad input"));
  }

  @Test
  void requestIdIsCarriedWhenProvided() {
    SmileIDException e = ErrorParser.parse(400, "{}", "req_123");
    assertEquals("req_123", e.getRequestId());
  }

  @Test
  void connectionAndLocalErrorsHaveNoHttpStatus() {
    assertNull(new ConnectionException("boom", null).getStatusCode());
    assertNull(new TimeoutException("too slow").getStatusCode());
    assertNull(new ValidationException("bad").getStatusCode());
  }
}
