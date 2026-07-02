package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;

/** HTTP 202 response from the entry endpoints (spec §5.2). */
public final class AcceptedResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("created_at")
  private String createdAt;

  /**
   * Normalized acceptance check: true when the raw status is "Accepted" or "accepted" in any
   * casing. Use this instead of branching on {@link #getStatus()}.
   */
  public boolean isAccepted() {
    return status != null && status.toLowerCase(Locale.ROOT).equals("accepted");
  }

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

  public String getCreatedAt() {
    return createdAt;
  }
}
