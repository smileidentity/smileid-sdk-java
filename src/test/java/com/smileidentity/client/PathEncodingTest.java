package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.smileidentity.generated.models.FraudReason;
import java.time.Instant;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fleet standard (2026-07-03): job_id and user_id path params are percent-encoded as single path
 * segments, so a hostile id cannot add path segments; golden ids stay byte-identical.
 */
class PathEncodingTest {

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

  @Test
  void goldenJobIdStaysByteIdenticalOnTheWire() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            200,
            "{\"status\":\"complete\",\"job_id\":\"job_01h2xcejqtf2nbrexx3vqjhp41\","
                + "\"user_id\":\"u\",\"message\":\"done\"}"));
    smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");
    server.takeRequest();
    assertEquals("/v3/status/job_01h2xcejqtf2nbrexx3vqjhp41", server.takeRequest().getPath());
  }

  @Test
  void hostileJobIdCannotAddPathSegments() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            404,
            "{\"status\":\"not_found\",\"job_id\":\"x\",\"user_id\":\"unknown\","
                + "\"message\":\"Verification not found\"}"));
    smile.verifications().retrieve("../admin/../x y");
    server.takeRequest();
    assertEquals("/v3/status/..%2Fadmin%2F..%2Fx%20y", server.takeRequest().getPath());
  }

  @Test
  void hostileUserIdIsEncodedAsASingleSegment() throws Exception {
    enqueueToken();
    server.enqueue(
        TestSupport.json(
            202,
            "{\"status\":\"accepted\",\"message\":\"Fraud report accepted\",\"user_id\":\"x\"}"));
    smile
        .users()
        .flagFraud(
            "user/../evil?x=1", FraudReason.ACCOUNT_TAKEOVER, null, "fraud-team@example.com");
    server.takeRequest();
    assertEquals("/v3/users/user%2F..%2Fevil%3Fx%3D1/report_fraud", server.takeRequest().getPath());
  }
}
