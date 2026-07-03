package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.errors.AuthenticationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Spec §2.3/§2A: internal JWT lifecycle. */
class TokenLifecycleTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void tokenRequestUsesLowercaseHeadersEmptyBodyAndTelemetry() throws Exception {
    String jwt = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.idStatusOk());

    TestSupport.client(server).services().idStatus("NG", "NIN");

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
    assertEquals("12.0.0", tokenReq.getHeader("SmileID-Source-SDK-Version"));
    assertNotNull(tokenReq.getHeader("User-Agent"));
    assertTrue(tokenReq.getHeader("User-Agent").startsWith("smileid-sdk-java/12.0.0 ("));

    RecordedRequest statusReq = server.takeRequest();
    assertEquals(jwt, statusReq.getHeader("SmileID-Token"));
    assertTrue(TestSupport.exactHeaderNames(statusReq).contains("SmileID-Token"));
    assertEquals("java", statusReq.getHeader("SmileID-Source-SDK"));
  }

  @Test
  void cachesTokenAcrossCallsUntilExpiryMinusSkew() throws Exception {
    String jwt = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.idStatusOk());
    server.enqueue(TestSupport.idStatusOk());

    SmileID smile = TestSupport.client(server);
    smile.services().idStatus("NG", "NIN");
    smile.services().idStatus("NG", "NIN");

    assertEquals(3, server.getRequestCount());
    assertEquals("/v3/token", server.takeRequest().getPath());
    assertTrue(server.takeRequest().getPath().startsWith("/v3/services/id_status"));
    assertTrue(server.takeRequest().getPath().startsWith("/v3/services/id_status"));
  }

  @Test
  void tokenExpiringWithinSkewIsRefetchedOnNextCall() throws Exception {
    // exp - 60s is already in the past, so the token is used once and refreshed next call.
    String shortLived = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 30);
    server.enqueue(TestSupport.tokenResponse(shortLived));
    server.enqueue(TestSupport.idStatusOk());
    server.enqueue(TestSupport.tokenResponse(shortLived));
    server.enqueue(TestSupport.idStatusOk());

    SmileID smile = TestSupport.client(server);
    smile.services().idStatus("NG", "NIN");
    smile.services().idStatus("NG", "NIN");

    assertEquals(4, server.getRequestCount());
    assertEquals("/v3/token", server.takeRequest().getPath());
    server.takeRequest();
    assertEquals("/v3/token", server.takeRequest().getPath());
  }

  @Test
  void undecodableTokenIsTreatedAsSingleUse() throws Exception {
    server.enqueue(TestSupport.tokenResponse("opaque-not-a-jwt"));
    server.enqueue(TestSupport.idStatusOk());
    server.enqueue(TestSupport.tokenResponse("opaque-not-a-jwt"));
    server.enqueue(TestSupport.idStatusOk());

    SmileID smile = TestSupport.client(server);
    smile.services().idStatus("NG", "NIN");
    smile.services().idStatus("NG", "NIN");

    assertEquals(4, server.getRequestCount());
  }

  @Test
  void refreshesTokenOnceOn401AndRetriesTheCall() throws Exception {
    String jwt1 = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    String jwt2 = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 7200);
    server.enqueue(TestSupport.tokenResponse(jwt1));
    server.enqueue(
        TestSupport.json(
            401,
            "{\"status\":\"Unauthorized\",\"message\":\"Invalid authentication credentials.\"}"));
    server.enqueue(TestSupport.tokenResponse(jwt2));
    server.enqueue(TestSupport.idStatusOk());

    TestSupport.client(server).services().idStatus("NG", "NIN");

    assertEquals(4, server.getRequestCount());
    server.takeRequest(); // token
    assertEquals(jwt1, server.takeRequest().getHeader("SmileID-Token"));
    assertEquals("/v3/token", server.takeRequest().getPath());
    assertEquals(jwt2, server.takeRequest().getHeader("SmileID-Token"));
  }

  @Test
  void secondConsecutive401RaisesAuthenticationError() throws Exception {
    String jwt = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    String unauthorized =
        "{\"status\":\"Unauthorized\",\"message\":\"Invalid authentication credentials.\"}";
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.json(401, unauthorized));
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.json(401, unauthorized));

    SmileID smile = TestSupport.client(server);
    AuthenticationException e =
        assertThrows(AuthenticationException.class, () -> smile.services().idStatus("NG", "NIN"));
    assertEquals(401, e.getStatusCode());
    assertEquals("Invalid authentication credentials.", e.getMessage());
    assertEquals(4, server.getRequestCount());
  }

  @Test
  void concurrentCallsDoNotStampedeTheTokenEndpoint() throws Exception {
    String jwt = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    server.enqueue(TestSupport.tokenResponse(jwt));
    int threads = 8;
    for (int i = 0; i < threads; i++) {
      server.enqueue(TestSupport.idStatusOk());
    }

    SmileID smile = TestSupport.client(server);
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      futures.add(
          pool.submit(
              () -> {
                ready.await();
                smile.services().idStatus("NG", "NIN");
                return null;
              }));
    }
    ready.countDown();
    for (Future<?> f : futures) {
      f.get();
    }
    pool.shutdown();

    int tokenRequests = 0;
    int total = server.getRequestCount();
    for (int i = 0; i < total; i++) {
      if ("/v3/token".equals(server.takeRequest().getPath())) {
        tokenRequests++;
      }
    }
    assertEquals(1, tokenRequests, "concurrent calls must share a single token fetch");
    assertEquals(threads + 1, total);
  }

  @Test
  void unauthenticatedServicesCallsNeverFetchOrSendAToken() throws Exception {
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"id_types\":[]}"));
    server.enqueue(TestSupport.json(200, "{\"valid_documents\":[]}"));

    SmileID smile = TestSupport.client(server);
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
