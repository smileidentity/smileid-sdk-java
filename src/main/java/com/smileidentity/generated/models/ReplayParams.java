package com.smileidentity.generated.models;

/** Optional multipart/form-data body for POST /v3/replay/{job_id} (spec §6.10 as corrected). */
public final class ReplayParams {

  private final String callbackUrl;

  private ReplayParams(Builder b) {
    this.callbackUrl = b.callbackUrl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public static final class Builder {
    private String callbackUrl;

    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public ReplayParams build() {
      return new ReplayParams(this);
    }
  }
}
