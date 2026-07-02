package com.smileidentity.client;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry policy (spec §2.6): idempotent operations only, on connection errors and HTTP 408, 429 and
 * 5xx. 409 is never retried. Exponential backoff with jitter; Retry-After wins when present.
 */
final class RetryPolicy {

  private static final long BASE_MILLIS = 250L;
  private static final long MAX_MILLIS = 10_000L;

  private RetryPolicy() {}

  static boolean retryableStatus(int status) {
    switch (status) {
      case 408:
      case 429:
      case 500:
      case 502:
      case 503:
      case 504:
        return true;
      default:
        return false; // 409 explicitly excluded
    }
  }

  static long delayMillis(int attempt, String retryAfterHeader) {
    if (retryAfterHeader != null) {
      try {
        return Math.max(0L, Long.parseLong(retryAfterHeader.trim()) * 1000L);
      } catch (NumberFormatException ignored) {
        // Fall through to backoff.
      }
    }
    long backoff = BASE_MILLIS * (1L << Math.min(attempt, 20));
    long jitter = ThreadLocalRandom.current().nextLong(100L);
    return Math.min(backoff + jitter, MAX_MILLIS);
  }
}
