package com.smileidentity.helpers;

import java.time.Duration;

/**
 * Options for {@code verifications().waitUntilComplete(...)} (spec §6.9). Defaults: poll every 2
 * seconds, give up after 60 seconds, and treat not_found as still pending.
 */
public final class WaitOptions {

  private static final WaitOptions DEFAULTS = builder().build();

  private final Duration interval;
  private final Duration timeout;
  private final boolean treatNotFoundAsPending;

  private WaitOptions(Builder b) {
    this.interval = b.interval;
    this.timeout = b.timeout;
    this.treatNotFoundAsPending = b.treatNotFoundAsPending;
  }

  public static WaitOptions defaults() {
    return DEFAULTS;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Duration getInterval() {
    return interval;
  }

  public Duration getTimeout() {
    return timeout;
  }

  public boolean isTreatNotFoundAsPending() {
    return treatNotFoundAsPending;
  }

  public static final class Builder {
    private Duration interval = Duration.ofSeconds(2);
    private Duration timeout = Duration.ofSeconds(60);
    private boolean treatNotFoundAsPending = true;

    public Builder interval(Duration interval) {
      this.interval = interval;
      return this;
    }

    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public Builder treatNotFoundAsPending(boolean treatNotFoundAsPending) {
      this.treatNotFoundAsPending = treatNotFoundAsPending;
      return this;
    }

    public WaitOptions build() {
      return new WaitOptions(this);
    }
  }
}
