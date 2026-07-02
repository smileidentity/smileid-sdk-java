package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.smileidentity.client.MultipartParser.TestPart;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Spec §5.3.3: document and document_back accept image/jpeg or image/png. PNG is detected from the
 * filename extension or magic bytes; selfie_image and liveness_images always stay image/jpeg; an
 * explicit content type always wins.
 */
class DocumentContentTypeTest {

  private static final byte[] PNG_MAGIC_BYTES = {
    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01, 0x02, 0x03
  };

  @TempDir Path tempDir;

  private MockWebServer server;
  private SmileID smile;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    smile = TestSupport.client(server);
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  private List<TestPart> submit(BinaryInput document, BinaryInput documentBack, BinaryInput selfie)
      throws Exception {
    server.enqueue(
        TestSupport.tokenResponse(TestSupport.jwtWithExp(Instant.now().getEpochSecond() + 3600)));
    server.enqueue(TestSupport.json(202, "{\"status\":\"accepted\"}"));
    List<BinaryInput> liveness = new ArrayList<>();
    for (int i = 1; i <= 6; i++) {
      liveness.add(BinaryInput.of(("frame-" + i).getBytes(StandardCharsets.UTF_8)));
    }
    DocumentVerificationParams.Builder builder =
        DocumentVerificationParams.builder()
            .selfieImage(selfie)
            .livenessImages(liveness)
            .document(document)
            .country("NG")
            .userDetails(
                UserDetails.builder()
                    .givenNames("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .build())
            .consent(Consent.granted(Instant.now(), "EN", "https://example.com/privacy"));
    if (documentBack != null) {
      builder.documentBack(documentBack);
    }
    smile.documents().verify(builder.build());
    server.takeRequest(); // token
    return MultipartParser.parse(server.takeRequest());
  }

  private static String baseType(String contentType) {
    int semicolon = contentType.indexOf(';');
    return (semicolon >= 0 ? contentType.substring(0, semicolon) : contentType).trim();
  }

  private static BinaryInput jpegSelfie() {
    return BinaryInput.of("fake-selfie".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void pngDocumentFilePathIsLabeledImagePng() throws Exception {
    File front = tempDir.resolve("id-front.png").toFile();
    File back = tempDir.resolve("id-back.png").toFile();
    Files.write(front.toPath(), "front-content".getBytes(StandardCharsets.UTF_8));
    Files.write(back.toPath(), "back-content".getBytes(StandardCharsets.UTF_8));

    List<TestPart> parts = submit(BinaryInput.of(front), BinaryInput.of(back), jpegSelfie());

    TestPart document = MultipartParser.single(parts, "document");
    assertEquals("image/png", baseType(document.contentType));
    assertEquals("id-front.png", document.filename);
    TestPart documentBack = MultipartParser.single(parts, "document_back");
    assertEquals("image/png", baseType(documentBack.contentType));
    assertEquals("id-back.png", documentBack.filename);
  }

  @Test
  void pngDocumentBytesAreDetectedByMagicBytes() throws Exception {
    List<TestPart> parts = submit(BinaryInput.of(PNG_MAGIC_BYTES), null, jpegSelfie());
    assertEquals("image/png", baseType(MultipartParser.single(parts, "document").contentType));
  }

  @Test
  void selfieAndLivenessStayJpegEvenForPngBytes() throws Exception {
    List<TestPart> parts =
        submit(
            BinaryInput.of("jpeg-document".getBytes(StandardCharsets.UTF_8)),
            null,
            BinaryInput.of(PNG_MAGIC_BYTES));
    assertEquals("image/jpeg", baseType(MultipartParser.single(parts, "selfie_image").contentType));
    for (TestPart frame : MultipartParser.byName(parts, "liveness_images")) {
      assertEquals("image/jpeg", baseType(frame.contentType));
    }
    assertEquals("image/jpeg", baseType(MultipartParser.single(parts, "document").contentType));
  }

  @Test
  void explicitContentTypeOverridesDetection() throws Exception {
    List<TestPart> parts =
        submit(
            BinaryInput.of(PNG_MAGIC_BYTES).withFilename("scan.png").withContentType("image/jpeg"),
            null,
            jpegSelfie());
    assertEquals("image/jpeg", baseType(MultipartParser.single(parts, "document").contentType));
  }
}
