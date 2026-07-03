package com.smileidentity.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/** Shared helpers for MockWebServer-backed tests. */
final class TestSupport {

  private TestSupport() {}

  static final String PARTNER_ID = "1234";
  static final String API_KEY = "fake-api-key-for-tests";

  static SmileID client(MockWebServer server) {
    return clientBuilder(server).build();
  }

  static SmileID.Builder clientBuilder(MockWebServer server) {
    // Pin to 127.0.0.1 so connection-failure tests are not affected by IPv6/IPv4 route fallback.
    return SmileID.builder()
        .partnerId(PARTNER_ID)
        .apiKey(API_KEY)
        .baseUrl("http://127.0.0.1:" + server.getPort() + "/")
        .allowInsecureBaseUrl(true);
  }

  /** Builds an unsigned JWT whose payload carries the given exp (epoch seconds). */
  static String jwtWithExp(long expEpochSeconds) {
    Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
    String header =
        enc.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    String payload =
        enc.encodeToString(("{\"exp\":" + expEpochSeconds + "}").getBytes(StandardCharsets.UTF_8));
    return header + "." + payload + ".sig";
  }

  static MockResponse tokenResponse(String jwt) {
    return new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"token\":\"" + jwt + "\"}");
  }

  static MockResponse json(int code, String body) {
    return new MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body);
  }

  static MockResponse idStatusOk() {
    return json(
        200,
        "{\"last_checked\":\"2026-04-14T12:30:00.000Z\",\"last_check_status\":\"success\","
            + "\"last_hour_success_rate\":\"95%\",\"last_known_status\":\"online\","
            + "\"last_check_success_rate\":\"90%\"}");
  }

  /** Exact header names as sent on the wire (Headers.names() is case-insensitive). */
  static List<String> exactHeaderNames(RecordedRequest request) {
    List<String> names = new ArrayList<>();
    for (int i = 0; i < request.getHeaders().size(); i++) {
      names.add(request.getHeaders().name(i));
    }
    return names;
  }
}
