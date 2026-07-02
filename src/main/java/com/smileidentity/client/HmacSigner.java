package com.smileidentity.client;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Optional HMAC request signing (spec §2.5). OFF unless a partner secret is configured.
 *
 * <p>Provisional construction, to be confirmed with the backend before production use: {@code
 * hex(HMAC_SHA256(key = partner_secret, message = timestamp + raw_request_body_bytes))}. Kept in
 * this single class so the construction can be corrected in one place.
 */
final class HmacSigner {

  private static final DateTimeFormatter ISO_MILLIS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final String partnerSecret;

  HmacSigner(String partnerSecret) {
    this.partnerSecret = partnerSecret;
  }

  boolean isEnabled() {
    return partnerSecret != null && !partnerSecret.isEmpty();
  }

  /** Returns {timestamp, signature} over the exact serialized body bytes. */
  String[] sign(byte[] bodyBytes) {
    String timestamp = ISO_MILLIS.format(Instant.now());
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(partnerSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
      if (bodyBytes != null) {
        mac.update(bodyBytes);
      }
      byte[] digest = mac.doFinal();
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(Character.forDigit((b >> 4) & 0xF, 16));
        hex.append(Character.forDigit(b & 0xF, 16));
      }
      return new String[] {timestamp, hex.toString()};
    } catch (Exception e) {
      throw new IllegalStateException("HMAC-SHA256 is unavailable on this JVM", e);
    }
  }
}
