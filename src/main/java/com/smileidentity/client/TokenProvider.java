package com.smileidentity.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Thread-safe JWT cache (spec §2.3/§2A ensure_token). The token is cached until its {@code exp}
 * claim minus a 60 second skew. If the claim cannot be decoded the token is treated as single-use
 * and refreshed on the next call.
 */
final class TokenProvider {

  interface Fetcher {
    String fetch();
  }

  private static final long SKEW_MILLIS = 60_000L;

  private final Fetcher fetcher;
  private String jwt;
  private long expiresAtMillis;

  TokenProvider(Fetcher fetcher) {
    this.fetcher = fetcher;
  }

  synchronized String ensureToken() {
    long now = System.currentTimeMillis();
    if (jwt != null && now < expiresAtMillis) {
      return jwt;
    }
    String fetched = fetcher.fetch();
    Long expMillis = decodeExpMillis(fetched);
    jwt = fetched;
    // Null exp means single-use: the cache is already expired for the next call.
    expiresAtMillis = expMillis != null ? expMillis - SKEW_MILLIS : now;
    return fetched;
  }

  synchronized void invalidate() {
    jwt = null;
    expiresAtMillis = 0;
  }

  static Long decodeExpMillis(String jwt) {
    try {
      String[] segments = jwt.split("\\.");
      if (segments.length < 2) {
        return null;
      }
      byte[] payload = Base64.getUrlDecoder().decode(segments[1]);
      JsonNode node = Json.mapper().readTree(new String(payload, StandardCharsets.UTF_8));
      JsonNode exp = node.get("exp");
      if (exp == null || !exp.canConvertToLong()) {
        return null;
      }
      return exp.asLong() * 1000L;
    } catch (Exception e) {
      return null;
    }
  }
}
