package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.errors.ApiException;
import com.smileidentity.errors.ConflictException;
import com.smileidentity.errors.ConnectionException;
import com.smileidentity.errors.RateLimitException;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.UserDetails;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Spec §2.6: retries for idempotent operations only; never 409; honour Retry-After. */
class RetryBehaviourTest {

  private MockWebServer server;

  private static final class RecordingSleeper implements Sleeper {
    final List<Long> delays = new ArrayList<>();

    @Override
    public void sleep(long millis) {
      delays.add(millis);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    server = TestSupport.tlsServer();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  private SmileID clientWith(RecordingSleeper sleeper) {
    SmileID smile = TestSupport.client(server);
    smile.transport().setSleeper(sleeper);
    return smile;
  }

  @Test
  void idempotentGetIsRetriedOn500ThenSucceeds() throws Exception {
    server.enqueue(TestSupport.json(500, "{\"error\":\"System Error\",\"code\":\"2401\"}"));
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    RecordingSleeper sleeper = new RecordingSleeper();

    clientWith(sleeper).services().bankCodes();

    assertEquals(2, server.getRequestCount());
    assertEquals(1, sleeper.delays.size());
  }

  @Test
  void idempotentGetIsRetriedOn408And429() throws Exception {
    server.enqueue(TestSupport.json(408, "{\"status\":\"Request Timeout\",\"message\":\"slow\"}"));
    server.enqueue(
        TestSupport.json(429, "{\"status\":\"Too Many Requests\",\"message\":\"slow down\"}"));
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));

    clientWith(new RecordingSleeper()).services().bankCodes();

    assertEquals(3, server.getRequestCount());
  }

  @Test
  void idempotentGetIsRetriedOnConnectionError() throws Exception {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));

    clientWith(new RecordingSleeper()).services().bankCodes();

    assertEquals(2, server.getRequestCount());
  }

  @Test
  void retryAfterHeaderIsHonoured() throws Exception {
    server.enqueue(
        TestSupport.json(429, "{\"status\":\"Too Many Requests\",\"message\":\"slow down\"}")
            .setHeader("Retry-After", "3"));
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    RecordingSleeper sleeper = new RecordingSleeper();

    clientWith(sleeper).services().bankCodes();

    assertEquals(1, sleeper.delays.size());
    assertEquals(3000L, sleeper.delays.get(0));
  }

  @Test
  void backoffGrowsExponentiallyWithJitter() throws Exception {
    server.enqueue(TestSupport.json(503, "{}"));
    server.enqueue(TestSupport.json(503, "{}"));
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    RecordingSleeper sleeper = new RecordingSleeper();

    clientWith(sleeper).services().bankCodes();

    assertEquals(2, sleeper.delays.size());
    assertTrue(
        sleeper.delays.get(0) >= 250 && sleeper.delays.get(0) < 400,
        "attempt 0: " + sleeper.delays);
    assertTrue(
        sleeper.delays.get(1) >= 500 && sleeper.delays.get(1) < 700,
        "attempt 1: " + sleeper.delays);
  }

  @Test
  void givesUpAfterMaxRetriesAndRaisesTheTypedError() throws Exception {
    for (int i = 0; i < 3; i++) {
      server.enqueue(TestSupport.json(500, "{\"error\":\"System Error\",\"code\":\"2401\"}"));
    }
    SmileID smile = clientWith(new RecordingSleeper());

    ApiException e = assertThrows(ApiException.class, () -> smile.services().bankCodes());
    assertEquals(500, e.getStatusCode());
    assertEquals("2401", e.getCode());
    assertEquals(3, server.getRequestCount(), "initial attempt + maxRetries (2)");
  }

  @Test
  void status409IsNeverRetriedEvenOnIdempotentOps() throws Exception {
    server.enqueue(TestSupport.json(409, "{\"status\":\"Conflict\",\"message\":\"conflict\"}"));
    SmileID smile = clientWith(new RecordingSleeper());

    assertThrows(ConflictException.class, () -> smile.services().bankCodes());
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void rateLimitSurfacesAfterRetriesExhausted() throws Exception {
    for (int i = 0; i < 3; i++) {
      server.enqueue(
          TestSupport.json(
              429,
              "{\"status\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"));
    }
    SmileID smile = clientWith(new RecordingSleeper());

    RateLimitException e =
        assertThrows(RateLimitException.class, () -> smile.services().bankCodes());
    assertEquals(429, e.getStatusCode());
  }

  @Test
  void tokenFetchIsRetried() throws Exception {
    server.enqueue(
        TestSupport.json(503, "{\"status\":\"Service Unavailable\",\"message\":\"down\"}"));
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.idStatusOk());

    clientWith(new RecordingSleeper()).services().idStatus("NG", "NIN");

    assertEquals(3, server.getRequestCount());
    assertEquals("/v3/token", server.takeRequest().getPath());
    assertEquals("/v3/token", server.takeRequest().getPath());
  }

  @Test
  void entryPostIsNeverRetriedOn500() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(
        TestSupport.json(
            500,
            "{\"status\":\"Internal Server Error\",\"message\":\"An unexpected error occurred. Please try again or contact support.\"}"));
    SmileID smile = clientWith(new RecordingSleeper());

    assertThrows(ApiException.class, () -> smile.enhancedKyc().verify(validKycParams()));
    assertEquals(2, server.getRequestCount(), "token + one entry attempt, no retry");
  }

  @Test
  void entryPostConnectionErrorSurfacesAsConnectionErrorWithoutRetry() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
    SmileID smile = clientWith(new RecordingSleeper());

    ConnectionException e =
        assertThrows(ConnectionException.class, () -> smile.enhancedKyc().verify(validKycParams()));
    assertTrue(e.getStatusCode() == null);
    assertEquals(2, server.getRequestCount(), "token + one entry attempt, no retry");
  }

  private static EnhancedKycParams validKycParams() {
    return EnhancedKycParams.builder()
        .country("NG")
        .idType("NIN")
        .idNumber("12345678901")
        .userDetails(
            UserDetails.builder()
                .givenNames("John")
                .lastName("Doe")
                .email("john@example.com")
                .build())
        .consent(Consent.granted("2026-03-06T12:00:00.000Z", "EN", "https://example.com/privacy"))
        .build();
  }
}
