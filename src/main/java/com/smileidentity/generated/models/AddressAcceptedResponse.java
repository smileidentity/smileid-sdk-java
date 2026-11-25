package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** HTTP 202 response from POST /v3/address. The result goes to the callback URL. */
public final class AddressAcceptedResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getJobId() {
    return jobId;
  }

  public String getUserId() {
    return userId;
  }
}
