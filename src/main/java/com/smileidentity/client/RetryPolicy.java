package com.smileidentity.client;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry policy (spec §2.6): idempotent operations only, on connection errors and HTTP 408, 429 and
 * 5xx. 409 is never retried. Exponential backoff with jitter; Retry-After wins when present, in
 * both its delta-seconds and RFC 7231 HTTP-date forms, capped at 60 seconds.
 */
final class RetryPolicy {

  private static final long BASE_MILLIS = 250L;
  private static final long MAX_BACKOFF_MILLIS = 10_000L;
  private static final long MAX_RETRY_AFTER_MILLIS = 60_000L;

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
    Long retryAfter = parseRetryAfterMillis(retryAfterHeader);
    if (retryAfter != null) {
      return Math.min(retryAfter, MAX_RETRY_AFTER_MILLIS);
    }
    long backoff = BASE_MILLIS * (1L << Math.min(attempt, 20));
    long jitter = ThreadLocalRandom.current().nextLong(100L);
    return Math.min(backoff + jitter, MAX_BACKOFF_MILLIS);
  }

  /** Parses Retry-After as delta seconds or an RFC 7231 HTTP-date; null when unparseable. */
  static Long parseRetryAfterMillis(String header) {
    if (header == null) {
      return null;
    }
    String value = header.trim();
    try {
      return Math.max(0L, Long.parseLong(value) * 1000L);
    } catch (NumberFormatException ignored) {
      // Not delta-seconds; try the HTTP-date form.
    }
    try {
      ZonedDateTime date = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
      return Math.max(0L, Duration.between(Instant.now(), date.toInstant()).toMillis());
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
