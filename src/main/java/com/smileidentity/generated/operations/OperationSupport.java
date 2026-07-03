package com.smileidentity.generated.operations;

import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;
import okhttp3.HttpUrl;

/** Shared param-to-part routing for the multipart operations (spec §2A build_multipart, §5.3). */
final class OperationSupport {

  static final String IMAGE_JPEG = "image/jpeg";

  private OperationSupport() {}

  /** callback_url precedence: per-request override, then params, then the client default. */
  static String effectiveCallbackUrl(
      Transport transport, String paramsCallbackUrl, RequestOptions options) {
    if (options != null && options.getCallbackUrl() != null) {
      validateCallbackUrl(options.getCallbackUrl());
      return options.getCallbackUrl();
    }
    if (paramsCallbackUrl != null) {
      validateCallbackUrl(paramsCallbackUrl);
      return paramsCallbackUrl;
    }
    return transport.defaultCallbackUrl();
  }

  static String pathSegment(String value) {
    return new HttpUrl.Builder()
        .scheme("https")
        .host("example.com")
        .addPathSegment(value)
        .build()
        .encodedPath()
        .substring(1);
  }

  private static void validateCallbackUrl(String value) {
    HttpUrl parsed = HttpUrl.parse(value);
    if (parsed == null || parsed.host().isEmpty()) {
      throw new ValidationException("callback_url must be an absolute URL");
    }
    if (!"https".equals(parsed.scheme())) {
      throw new ValidationException("callback_url must use https");
    }
  }

  static void addText(List<Part> parts, String name, String value) {
    if (value != null) {
      parts.add(Part.text(name, value));
    }
  }

  static void addBoolean(List<Part> parts, String name, Boolean value) {
    if (value != null) {
      parts.add(Part.text(name, value ? "true" : "false"));
    }
  }

  static void addNumber(List<Part> parts, String name, Number value) {
    if (value != null) {
      parts.add(Part.text(name, String.valueOf(value)));
    }
  }

  static void addJson(List<Part> parts, String name, Object value) {
    if (value != null) {
      parts.add(Part.json(name, value));
    }
  }

  static void addBinary(
      List<Part> parts, String name, BinaryInput input, String defaultFilename, String defaultCt) {
    if (input != null) {
      parts.add(Part.binary(name, input, defaultFilename, defaultCt));
    }
  }

  /**
   * Binary part for fields accepting JPEG or PNG (document, document_back — spec §5.3.3): PNG is
   * detected from the filename extension or magic bytes; an explicit content type still wins.
   */
  static void addDocumentBinary(
      List<Part> parts, String name, BinaryInput input, String defaultFilename) {
    if (input != null) {
      parts.add(Part.binary(name, input, defaultFilename, input.detectContentType(IMAGE_JPEG)));
    }
  }

  /** Repeated parts, all named {@code liveness_images} — never CSV or indexed (spec §5.3.4). */
  static void addLivenessImages(List<Part> parts, List<BinaryInput> images) {
    if (images == null) {
      return;
    }
    int index = 1;
    for (BinaryInput image : images) {
      parts.add(Part.binary("liveness_images", image, "liveness_" + index + ".jpg", IMAGE_JPEG));
      index++;
    }
  }

  static void addUserDetailsAndConsent(List<Part> parts, UserDetails userDetails, Consent consent) {
    addJson(parts, "user_details", userDetails);
    addJson(parts, "consent", consent);
  }

  static void addPartnerParamsAndMetadata(
      List<Part> parts, Map<String, String> partnerParams, List<MetadataEntry> metadata) {
    addJson(parts, "partner_params", partnerParams);
    addJson(parts, "metadata", metadata);
  }
}
