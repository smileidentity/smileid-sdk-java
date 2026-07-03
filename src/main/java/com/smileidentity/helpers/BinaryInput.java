package com.smileidentity.helpers;

import com.smileidentity.errors.ValidationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;

/**
 * A binary request input (selfie, liveness frame, document or comparison image). Accepts a {@link
 * File}, a {@code byte[]} or an {@link InputStream} (spec §5.3/§8). Streams are read fully when the
 * request is built so requests stay repeatable for the single 401 token refresh.
 */
public final class BinaryInput {

  private static final byte[] PNG_SIGNATURE = {
    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
  };

  private final File file;
  private final byte[] bytes;
  private final InputStream stream;
  private final String filename;
  private final String contentType;
  private byte[] cachedBytes;

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

  /** Reads the full content. Cached so detection and serialization share one read. */
  public byte[] readBytes() throws IOException {
    if (bytes != null) {
      return bytes;
    }
    if (cachedBytes != null) {
      return cachedBytes;
    }
    if (file != null) {
      cachedBytes = Files.readAllBytes(file.toPath());
      return cachedBytes;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = stream.read(buf)) != -1) {
      out.write(buf, 0, n);
    }
    cachedBytes = out.toByteArray();
    return cachedBytes;
  }

  /**
   * Content type for fields that accept JPEG or PNG (document, document_back): an explicit {@link
   * #withContentType} wins, then PNG detection by filename extension or magic bytes, then the given
   * fallback.
   */
  public String detectContentType(String fallback) {
    if (contentType != null) {
      return contentType;
    }
    if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".png")) {
      return "image/png";
    }
    try {
      if (isPng(readBytes())) {
        return "image/png";
      }
    } catch (IOException ignored) {
      // Unreadable input surfaces later, when the request body is built.
    }
    return fallback;
  }

  private static boolean isPng(byte[] data) {
    return data.length >= PNG_SIGNATURE.length && startsWith(data, PNG_SIGNATURE);
  }

  private static boolean startsWith(byte[] data, byte[] prefix) {
    for (int i = 0; i < prefix.length; i++) {
      if (data[i] != prefix[i]) {
        return false;
      }
    }
    return true;
  }
}
