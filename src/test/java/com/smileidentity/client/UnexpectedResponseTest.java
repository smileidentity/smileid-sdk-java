package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.errors.UnexpectedResponseException;
import com.smileidentity.generated.models.JobStatus;
import java.time.Instant;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fleet standard (2026-07-03): a success-path (2xx) response body that is not a JSON object raises
 * UnexpectedResponseException with statusCode, rawBody and requestId populated.
 */
class UnexpectedResponseTest {

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

  @Test
  void nonJsonSuccessBodyRaisesUnexpectedResponse() {
    server.enqueue(
        TestSupport.json(200, "<html>gateway interference</html>")
            .setHeader("X-Request-ID", "req_42"));
    UnexpectedResponseException e =
        assertThrows(UnexpectedResponseException.class, () -> smile.services().bankCodes());
    assertEquals(200, e.getStatusCode());
    assertEquals("<html>gateway interference</html>", e.getRawBody());
    assertEquals("req_42", e.getRequestId());
  }

  @Test
  void jsonArraySuccessBodyRaisesUnexpectedResponse() {
    server.enqueue(TestSupport.json(200, "[1,2,3]"));
    UnexpectedResponseException e =
        assertThrows(UnexpectedResponseException.class, () -> smile.services().bankCodes());
    assertEquals(200, e.getStatusCode());
    assertEquals("[1,2,3]", e.getRawBody());
  }

  @Test
  void emptySuccessBodyRaisesUnexpectedResponse() {
    server.enqueue(TestSupport.json(200, ""));
    UnexpectedResponseException e =
        assertThrows(UnexpectedResponseException.class, () -> smile.services().bankCodes());
    assertEquals(200, e.getStatusCode());
    assertTrue(e.getMessage().contains("empty body"));
  }

  @Test
  void retrieve404StillReturnsNotFoundJobStatus() {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(
        TestSupport.json(
            404,
            "{\"status\":\"not_found\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"unknown\",\"message\":\"Verification not found\"}"));
    JobStatus status = smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");
    assertTrue(status.isNotFound());
  }
}
