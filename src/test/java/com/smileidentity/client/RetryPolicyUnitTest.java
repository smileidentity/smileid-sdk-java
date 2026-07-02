package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

/** Spec §2.6: Retry-After handling — delta seconds, RFC 7231 HTTP-date, and the 60 second cap. */
class RetryPolicyUnitTest {

  @Test
  void deltaSecondsAreHonoured() {
    assertEquals(3000L, RetryPolicy.delayMillis(0, "3"));
  }

  @Test
  void honouredDelayIsCappedAtSixtySeconds() {
    assertEquals(60_000L, RetryPolicy.delayMillis(0, "120"));
  }

  @Test
  void httpDateFormIsHonoured() {
    String header =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30));
    long delay = RetryPolicy.delayMillis(0, header);
    assertTrue(delay > 20_000L && delay <= 30_000L, "expected roughly 30s, got " + delay);
  }

  @Test
  void httpDateFormIsCappedAtSixtySeconds() {
    String header =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(600));
    assertEquals(60_000L, RetryPolicy.delayMillis(0, header));
  }

  @Test
  void pastHttpDateMeansNoDelay() {
    String header =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(30));
    assertEquals(0L, RetryPolicy.delayMillis(0, header));
  }

  @Test
  void unparseableHeaderFallsBackToBackoff() {
    assertNull(RetryPolicy.parseRetryAfterMillis("soon"));
    long delay = RetryPolicy.delayMillis(0, "soon");
    assertTrue(delay >= 250L && delay < 400L, "expected first backoff window, got " + delay);
  }
}
