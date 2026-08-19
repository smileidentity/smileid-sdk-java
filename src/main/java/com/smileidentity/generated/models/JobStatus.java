package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from GET /v3/status/{jobId} (spec §5.2, §6.8). A 404 from that endpoint also returns
 * this shape with status "not_found" and is never raised as an error.
 *
 * <p>While a job runs, {@code status} is "processing". When it finishes, {@code status} carries the
 * decision itself — "clear", "block", "attention" or "error" — and {@code message} is a plain
 * sentence such as "Job completed". There is no literal "complete" status: the decision is in
 * {@code status}, not in {@code message}.
 */
public final class JobStatus {

  public static final String PROCESSING = "processing";
  public static final String NOT_FOUND = "not_found";

  @JsonProperty("status")
  private String status;

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("message")
  private String message;

  /**
   * True once the job has reached a decision — any status that is neither "processing" nor
   * "not_found". Read {@link #getStatus()} for the decision itself.
   */
  public boolean isComplete() {
    return status != null && !status.trim().isEmpty() && !isProcessing() && !isNotFound();
  }

  public boolean isProcessing() {
    return PROCESSING.equals(status);
  }

  public boolean isNotFound() {
    return NOT_FOUND.equals(status);
  }

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
