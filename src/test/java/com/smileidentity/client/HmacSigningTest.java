package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Spec §2.5: optional HMAC request signing, OFF unless partner_secret is configured. */
class HmacSigningTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = TestSupport.tlsServer();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void hmacHeadersAreAbsentWithoutPartnerSecret() throws Exception {
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    TestSupport.client(server).services().bankCodes();
    RecordedRequest r = server.takeRequest();
    assertNull(r.getHeader("SmileID-Timestamp"));
    assertNull(r.getHeader("SmileID-Request-Signature"));
  }

  @Test
  void hmacHeadersAreSetAndVerifiableWhenSecretConfigured() throws Exception {
    String secret = "test-partner-secret";
    server.enqueue(TestSupport.json(200, "{\"bank_codes\":[]}"));
    TestSupport.clientBuilder(server).partnerSecret(secret).build().services().bankCodes();

    RecordedRequest r = server.takeRequest();
    String ts = r.getHeader("SmileID-Timestamp");
    String sig = r.getHeader("SmileID-Request-Signature");
    assertNotNull(ts);
    assertNotNull(sig);
    // ISO 8601 UTC with milliseconds.
    assertTrue(ts.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"), ts);
    // Provisional construction: hex(HMAC_SHA256(secret, timestamp + raw_body)); GET body is empty.
    byte[] body = r.getBody().readByteArray();
    assertEquals(expectedSignature(secret, ts, body), sig);
  }

  @Test
  void signatureCoversTheExactSerializedBodyBytes() throws Exception {
    String secret = "test-partner-secret";
    String jwt = TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600);
    server.enqueue(TestSupport.tokenResponse(jwt));
    server.enqueue(TestSupport.idStatusOk());

    TestSupport.clientBuilder(server)
        .partnerSecret(secret)
        .build()
        .services()
        .idStatus("NG", "NIN");

    // Both the token POST and the GET are signed when the secret is set.
    RecordedRequest tokenReq = server.takeRequest();
    String ts = tokenReq.getHeader("SmileID-Timestamp");
    assertNotNull(ts);
    assertEquals(
        expectedSignature(secret, ts, tokenReq.getBody().readByteArray()),
        tokenReq.getHeader("SmileID-Request-Signature"));
  }

  private static String expectedSignature(String secret, String timestamp, byte[] body)
      throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
    mac.update(body);
    StringBuilder hex = new StringBuilder();
    for (byte b : mac.doFinal()) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }
}
