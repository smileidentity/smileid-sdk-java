package com.smileidentity.client;

import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.generated.models.ReplayCallbackResponse;
import com.smileidentity.generated.models.ReplayParams;
import com.smileidentity.generated.operations.VerificationOperations;
import com.smileidentity.helpers.JobPoller;
import com.smileidentity.helpers.WaitOptions;

/** {@code client.verifications()} — job status and callback replay (spec §4, §6.8–§6.10). */
public final class VerificationsResource {

  private final Transport transport;

  VerificationsResource(Transport transport) {
    this.transport = transport;
  }

  /**
   * GET /v3/status/{jobId}. A 404 returns a JobStatus with status "not_found" instead of raising
   * (spec §6.8).
   */
  public JobStatus retrieve(String jobId) {
    return retrieve(jobId, RequestOptions.none());
  }

  public JobStatus retrieve(String jobId, RequestOptions options) {
    return VerificationOperations.status(transport, jobId, options);
  }

  /**
   * Polls {@link #retrieve(String)} until the job completes (spec §6.9). Defaults: 2 second
   * interval, 60 second timeout, not_found treated as pending. Raises {@link
   * com.smileidentity.errors.TimeoutException} at the deadline.
   */
  public JobStatus waitUntilComplete(String jobId) {
    return waitUntilComplete(jobId, WaitOptions.defaults());
  }

  public JobStatus waitUntilComplete(String jobId, WaitOptions options) {
    return JobPoller.waitUntilComplete(jobId, () -> retrieve(jobId), options);
  }

  /** POST /v3/replay/{job_id} — JSON body, never auto-retried (spec §6.10). */
  public ReplayCallbackResponse replay(String jobId) {
    return replay(jobId, null, RequestOptions.none());
  }

  public ReplayCallbackResponse replay(String jobId, ReplayParams params) {
    return replay(jobId, params, RequestOptions.none());
  }

  public ReplayCallbackResponse replay(String jobId, ReplayParams params, RequestOptions options) {
    return VerificationOperations.replay(transport, jobId, params, options);
  }
}
