package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.errors.TimeoutException;
import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.helpers.WaitOptions;
import java.time.Duration;
import java.time.Instant;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Spec §6.9: wait_until_complete polls jobs.retrieve until terminal or deadline. */
class WaitUntilCompleteTest {

  private static final String JOB_ID = "job_01h2xcejqtf2nbrexx3vqjhp41";

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

  private static String statusBody(String status) {
    return "{\"status\":\""
        + status
        + "\",\"job_id\":\""
        + JOB_ID
        + "\",\"user_id\":\"user_01h8x9y2z3a4b5c6d7e8f9g0h1\",\"message\":\"Job completed\"}";
  }

  private void enqueueToken() {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
  }

  @Test
  void pollsUntilCompleteAndReturnsTheTerminalStatus() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, statusBody("processing")));
    server.enqueue(TestSupport.json(202, statusBody("processing")));
    server.enqueue(TestSupport.json(200, statusBody("clear")));

    JobStatus status =
        smile
            .verifications()
            .waitUntilComplete(
                JOB_ID,
                WaitOptions.builder()
                    .interval(Duration.ofMillis(5))
                    .timeout(Duration.ofSeconds(5))
                    .build());

    assertTrue(status.isComplete());
    assertEquals("clear", status.getStatus());
    assertEquals(4, server.getRequestCount(), "token + three polls");
  }

  @Test
  void returnsOnBlockAsWellAsClear() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(202, statusBody("processing")));
    server.enqueue(TestSupport.json(200, statusBody("block")));

    JobStatus status =
        smile
            .verifications()
            .waitUntilComplete(
                JOB_ID,
                WaitOptions.builder()
                    .interval(Duration.ofMillis(5))
                    .timeout(Duration.ofSeconds(5))
                    .build());

    assertTrue(status.isComplete());
    assertEquals("block", status.getStatus());
    assertEquals(3, server.getRequestCount(), "token + processing poll + block poll");
  }

  @Test
  void raisesTimeoutErrorWhenTheDeadlinePasses() throws Exception {
    enqueueToken();
    // Serve "processing" forever.
    server.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            if (request.getPath().equals("/v3/token")) {
              return new MockResponse()
                  .setResponseCode(200)
                  .setHeader("Content-Type", "application/json")
                  .setBody(
                      "{\"token\":\""
                          + TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)
                          + "\"}");
            }
            return new MockResponse()
                .setResponseCode(202)
                .setHeader("Content-Type", "application/json")
                .setBody(statusBody("processing"));
          }
        });

    TimeoutException e =
        assertThrows(
            TimeoutException.class,
            () ->
                smile
                    .verifications()
                    .waitUntilComplete(
                        JOB_ID,
                        WaitOptions.builder()
                            .interval(Duration.ofMillis(10))
                            .timeout(Duration.ofMillis(60))
                            .build()));
    assertTrue(e.getMessage().contains(JOB_ID));
  }

  @Test
  void notFoundIsTreatedAsPendingByDefault() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(404, statusBody("not_found")));
    server.enqueue(TestSupport.json(200, statusBody("clear")));

    JobStatus status =
        smile
            .verifications()
            .waitUntilComplete(
                JOB_ID,
                WaitOptions.builder()
                    .interval(Duration.ofMillis(5))
                    .timeout(Duration.ofSeconds(5))
                    .build());

    assertTrue(status.isComplete());
    assertEquals(3, server.getRequestCount(), "token + not_found poll + clear poll");
  }

  @Test
  void notFoundReturnsImmediatelyWhenNotTreatedAsPending() throws Exception {
    enqueueToken();
    server.enqueue(TestSupport.json(404, statusBody("not_found")));

    JobStatus status =
        smile
            .verifications()
            .waitUntilComplete(
                JOB_ID,
                WaitOptions.builder()
                    .interval(Duration.ofMillis(5))
                    .timeout(Duration.ofSeconds(5))
                    .treatNotFoundAsPending(false)
                    .build());

    assertTrue(status.isNotFound());
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void defaultOptionsExist() {
    WaitOptions defaults = WaitOptions.defaults();
    assertEquals(Duration.ofSeconds(2), defaults.getInterval());
    assertEquals(Duration.ofSeconds(60), defaults.getTimeout());
    assertTrue(defaults.isTreatNotFoundAsPending());
  }
}
