package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fleet standard (2026-07-03): multipart filename and content-type values must be safe against
 * header injection (CR, LF, quote) on every path, including caller-supplied explicit values.
 */
class HostileFilenameTest {

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

  private static EnrollParams enrollParams(BinaryInput selfie) {
    List<BinaryInput> liveness = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      liveness.add(BinaryInput.of(new byte[] {1, 2, 3}));
    }
    return EnrollParams.builder()
        .selfieImage(selfie)
        .livenessImages(liveness)
        .userDetails(
            UserDetails.builder()
                .givenNames("John")
                .lastName("Doe")
                .email("john@example.com")
                .build())
        .consent(Consent.granted(Instant.now(), "EN", "https://example.com/privacy"))
        .build();
  }

  @Test
  void crLfAndQuoteInFilenameAreEscapedOnTheWire() throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.json(202, "{\"status\":\"Accepted\"}"));

    String hostile = "evil\"\r\nX-Injected: 1\r\n\r\n.jpg";
    smile
        .biometric()
        .enroll(
            enrollParams(
                BinaryInput.of("selfie-bytes".getBytes(StandardCharsets.UTF_8))
                    .withFilename(hostile)));

    server.takeRequest(); // token
    RecordedRequest request = server.takeRequest();
    String rawBody = new String(request.getBody().readByteArray(), StandardCharsets.UTF_8);
    // The hostile filename must not create new header lines: CR and LF are escaped, so no line
    // in the multipart body may start with the injected header name.
    for (String line : rawBody.split("\r\n")) {
      assertFalse(line.startsWith("X-Injected"), "injected header line found: " + line);
    }
    // The raw quote must be escaped too, so the value cannot break out of the quoted string.
    assertFalse(rawBody.contains("evil\""), "quote must be escaped");
    // OkHttp escapes the dangerous characters as %0D, %0A and %22 inside the quoted string.
    assertTrue(
        rawBody.contains("filename=\"evil%22%0D%0AX-Injected: 1%0D%0A%0D%0A.jpg\""),
        "expected escaped filename in: " + firstDispositionLine(rawBody));
  }

  @Test
  void hostileExplicitContentTypeIsRejectedBeforeAnyRequest() {
    String hostileContentType = "image/jpeg\r\nX-Evil: yes";
    ValidationException e =
        assertThrows(
            ValidationException.class,
            () ->
                smile
                    .biometric()
                    .enroll(
                        enrollParams(
                            BinaryInput.of("selfie-bytes".getBytes(StandardCharsets.UTF_8))
                                .withContentType(hostileContentType))));
    assertTrue(e.getMessage().contains("selfie_image"));
    assertEquals(0, server.getRequestCount(), "nothing may be sent, not even a token fetch");
  }

  private static String firstDispositionLine(String body) {
    for (String line : body.split("\r\n")) {
      if (line.startsWith("Content-Disposition") && line.contains("filename")) {
        return line;
      }
    }
    return "(no disposition line found)";
  }
}
