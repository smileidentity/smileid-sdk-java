package com.smileidentity.generated.operations;

import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import com.smileidentity.helpers.Validators;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Shared param-to-part routing for the multipart operations (spec §2A build_multipart, §5.3). */
final class OperationSupport {

  static final String IMAGE_JPEG = "image/jpeg";

  private OperationSupport() {}

  /**
   * callback_url precedence: per-request override, then params, then the client default. The
   * effective value must be https (fleet standard, 2026-07-03); validated before any request is
   * sent.
   */
  static String effectiveCallbackUrl(
      Transport transport, String paramsCallbackUrl, RequestOptions options) {
    String resolved;
    if (options != null && options.getCallbackUrl() != null) {
      resolved = options.getCallbackUrl();
    } else if (paramsCallbackUrl != null) {
      resolved = paramsCallbackUrl;
    } else {
      resolved = transport.defaultCallbackUrl();
    }
    Validators.requireHttpsCallbackUrl(resolved, "callbackUrl");
    return resolved;
  }

  /**
   * Percent-encodes a path parameter as a single URL path segment (fleet standard, 2026-07-03): RFC
   * 3986 unreserved characters pass through, everything else — including "/" — is encoded, so a
   * hostile id cannot add path segments. Golden ids ([0-9a-z_-]) are unchanged.
   */
  static String encodePathSegment(String value) {
    StringBuilder encoded = new StringBuilder(value.length());
    for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
      char c = (char) (b & 0xFF);
      boolean unreserved =
          (c >= 'A' && c <= 'Z')
              || (c >= 'a' && c <= 'z')
              || (c >= '0' && c <= '9')
              || c == '-'
              || c == '.'
              || c == '_'
              || c == '~';
      if (unreserved) {
        encoded.append(c);
      } else {
        encoded.append('%');
        encoded.append(Character.toUpperCase(Character.forDigit((c >> 4) & 0xF, 16)));
        encoded.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
      }
    }
    return encoded.toString();
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
