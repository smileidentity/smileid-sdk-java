package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from POST /v3/replay/{job_id} (spec §6.10). */
public final class ReplayCallbackResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("message")
  private String message;

  public String getStatus() {
    return status;
  }

  public String getJobId() {
    return jobId;
  }

  public String getUserId() {
    return userId;
  }

  public String getMessage() {
    return message;
  }
}
