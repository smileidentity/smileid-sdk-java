package com.smileidentity.helpers;

import com.smileidentity.errors.TimeoutException;
import com.smileidentity.generated.models.JobStatus;
import java.util.function.Supplier;

/** Polling loop behind {@code verifications().waitUntilComplete(...)} (spec §6.9). */
public final class JobPoller {

  private JobPoller() {}

  /**
   * Polls {@code retrieve} while the job is "processing" (and while it is "not_found", when that is
   * treated as pending), and returns as soon as the status is anything else — the decision, for
   * example "clear" or "block". Raises {@link TimeoutException} when the deadline passes.
   */
  public static JobStatus waitUntilComplete(
      String jobId, Supplier<JobStatus> retrieve, WaitOptions options) {
    WaitOptions opts = options == null ? WaitOptions.defaults() : options;
    long deadline = System.nanoTime() + opts.getTimeout().toNanos();
    while (true) {
      JobStatus status = retrieve.get();
      if (status != null && status.isComplete()) {
        return status;
      }
      if (status != null && status.isNotFound() && !opts.isTreatNotFoundAsPending()) {
        return status;
      }
      if (System.nanoTime() >= deadline) {
        throw new TimeoutException(
            "Timed out after "
                + opts.getTimeout().toMillis()
                + "ms waiting for job "
                + jobId
                + " to complete");
      }
      try {
        Thread.sleep(Math.max(1L, opts.getInterval().toMillis()));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new TimeoutException("Interrupted while waiting for job " + jobId + " to complete");
      }
    }
  }
}
