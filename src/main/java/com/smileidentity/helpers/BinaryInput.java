package com.smileidentity.helpers;

import com.smileidentity.errors.ValidationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * A binary request input (selfie, liveness frame, document or comparison image). Accepts a {@link
 * File}, a {@code byte[]} or an {@link InputStream} (spec §5.3/§8). Streams are read fully when the
 * request is built so requests stay repeatable for signing and 401 refresh.
 */
public final class BinaryInput {

  private final File file;
  private final byte[] bytes;
  private final InputStream stream;
  private final String filename;
  private final String contentType;

  private BinaryInput(File file, byte[] bytes, InputStream stream, String filename, String ct) {
    this.file = file;
    this.bytes = bytes;
    this.stream = stream;
    this.filename = filename;
    this.contentType = ct;
  }

  public static BinaryInput of(File file) {
    if (file == null) {
      throw new ValidationException("file must not be null");
    }
    return new BinaryInput(file, null, null, file.getName(), null);
  }

  public static BinaryInput of(byte[] bytes) {
    if (bytes == null) {
      throw new ValidationException("bytes must not be null");
    }
    return new BinaryInput(null, bytes, null, null, null);
  }

  public static BinaryInput of(InputStream stream) {
    if (stream == null) {
      throw new ValidationException("stream must not be null");
    }
    return new BinaryInput(null, null, stream, null, null);
  }

  /** Returns a copy with an explicit part filename. */
  public BinaryInput withFilename(String filename) {
    return new BinaryInput(file, bytes, stream, filename, contentType);
  }

  /** Returns a copy with an explicit content type (e.g. "image/png"). */
  public BinaryInput withContentType(String contentType) {
    return new BinaryInput(file, bytes, stream, filename, contentType);
  }

  /** Filename to use for the multipart part, or null to use the per-field default. */
  public String getFilename() {
    return filename;
  }

  /** Content type for the multipart part, or null to use the per-field default. */
  public String getContentType() {
    return contentType;
  }

  /** Reads the full content. Called once per request build. */
  public byte[] readBytes() throws IOException {
    if (bytes != null) {
      return bytes;
    }
    if (file != null) {
      return Files.readAllBytes(file.toPath());
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = stream.read(buf)) != -1) {
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }
}
