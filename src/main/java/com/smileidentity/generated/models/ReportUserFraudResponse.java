package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from POST /v3/users/{user_id}/report_fraud (spec §6.11). */
public final class ReportUserFraudResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("user_id")
  private String userId;

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getUserId() {
    return userId;
  }
}
