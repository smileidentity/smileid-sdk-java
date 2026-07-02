package com.smileidentity.generated.operations;

import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.MetadataEntry;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/** Shared param-to-part routing for the multipart operations (spec §2A build_multipart, §5.3). */
final class OperationSupport {

  static final String IMAGE_JPEG = "image/jpeg";

  private OperationSupport() {}

  /** callback_url precedence: per-request override, then params, then the client default. */
  static String effectiveCallbackUrl(
      Transport transport, String paramsCallbackUrl, RequestOptions options) {
    if (options != null && options.getCallbackUrl() != null) {
      return options.getCallbackUrl();
    }
    if (paramsCallbackUrl != null) {
      return paramsCallbackUrl;
    }
    return transport.defaultCallbackUrl();
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
