package com.smileidentity.client;

import com.smileidentity.helpers.BinaryInput;

/** One multipart part description (spec §5.3). Assembled into OkHttp form data by the transport. */
public final class Part {

  public enum Kind {
    TEXT,
    JSON,
    BINARY
  }

  private final Kind kind;
  private final String name;
  private final String textValue;
  private final Object jsonValue;
  private final BinaryInput binary;
  private final String defaultFilename;
  private final String defaultContentType;

  private Part(
      Kind kind,
      String name,
      String textValue,
      Object jsonValue,
      BinaryInput binary,
      String defaultFilename,
      String defaultContentType) {
    this.kind = kind;
    this.name = name;
    this.textValue = textValue;
    this.jsonValue = jsonValue;
    this.binary = binary;
    this.defaultFilename = defaultFilename;
    this.defaultContentType = defaultContentType;
  }

  /** Scalar text part. Booleans arrive as "true"/"false", numbers as decimal strings. */
  public static Part text(String name, String value) {
    return new Part(Kind.TEXT, name, value, null, null, null, null);
  }

  /** JSON object or array part, serialized with Content-Type: application/json. */
  public static Part json(String name, Object value) {
    return new Part(Kind.JSON, name, null, value, null, null, null);
  }

  /** Binary part with a filename and content type. */
  public static Part binary(
      String name, BinaryInput input, String defaultFilename, String defaultContentType) {
    return new Part(Kind.BINARY, name, null, null, input, defaultFilename, defaultContentType);
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public String getTextValue() {
    return textValue;
  }

  public Object getJsonValue() {
    return jsonValue;
  }

  public BinaryInput getBinary() {
    return binary;
  }

  public String getDefaultFilename() {
    return defaultFilename;
  }

  public String getDefaultContentType() {
    return defaultContentType;
  }
}
