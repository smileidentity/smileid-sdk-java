package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.generated.models.ReplayCallbackResponse;
import com.smileidentity.generated.models.ReplayParams;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin operation functions for GET /v3/status/{jobId} (idempotent, retryable; 404 returns a
 * JobStatus body) and POST /v3/replay/{job_id} (JSON body, never retried) — spec §6.8, §6.10.
 */
public final class VerificationOperations {

  private VerificationOperations() {}

  public static JobStatus status(Transport transport, String jobId, RequestOptions options) {
    ApiRequest request =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/status/" + jobId)
            .authenticated(true)
            .idempotent(true)
            .notFoundReturnsBody(true)
            .options(options)
            .build();
    return transport.execute(request, JobStatus.class);
  }

  public static ReplayCallbackResponse replay(
      Transport transport, String jobId, ReplayParams params, RequestOptions options) {
    Map<String, String> body = new LinkedHashMap<>();
    if (params != null && params.getCallbackUrl() != null) {
      body.put("callback_url", params.getCallbackUrl());
    }
    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/replay/" + jobId)
            .authenticated(true)
            .jsonBody(body)
            .options(options)
            .build();
    return transport.execute(request, ReplayCallbackResponse.class);
  }
}
