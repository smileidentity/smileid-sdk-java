package com.smileidentity.client;

import java.time.Duration;

/** Per-request options: total timeout override and callback URL override (spec §4). */
public final class RequestOptions {

  private static final RequestOptions NONE = builder().build();

  private final Duration timeout;
  private final String callbackUrl;

  private RequestOptions(Builder b) {
    this.timeout = b.timeout;
    this.callbackUrl = b.callbackUrl;
  }

  public static RequestOptions none() {
    return NONE;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Per-request total timeout, or null to use the client default. */
  public Duration getTimeout() {
    return timeout;
  }

  /** Callback URL override for this call, or null. */
  public String getCallbackUrl() {
    return callbackUrl;
  }

  public static final class Builder {
    private Duration timeout;
    private String callbackUrl;

    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
