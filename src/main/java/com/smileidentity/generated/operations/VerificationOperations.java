package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.generated.models.ReplayCallbackResponse;
import com.smileidentity.generated.models.ReplayParams;
import com.smileidentity.helpers.Validators;
import java.util.Collections;

/**
 * Thin operation functions for GET /v3/status/{jobId} (idempotent, retryable; 404 returns a
 * JobStatus body) and POST /v3/replay/{job_id} (optional multipart body, never retried) — spec
 * §6.8, §6.10 as corrected: replay takes multipart/form-data, any other content type is 415.
 */
public final class VerificationOperations {

  private VerificationOperations() {}

  public static JobStatus status(Transport transport, String jobId, RequestOptions options) {
    ApiRequest request =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/status/" + OperationSupport.encodePathSegment(jobId))
            .authenticated(true)
            .idempotent(true)
            .notFoundReturnsBody(true)
            .options(options)
            .build();
    return transport.execute(request, JobStatus.class);
  }

  public static ReplayCallbackResponse replay(
      Transport transport, String jobId, ReplayParams params, RequestOptions options) {
    ApiRequest.Builder builder =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/replay/" + OperationSupport.encodePathSegment(jobId))
            .authenticated(true)
            .options(options);
    if (params != null && params.getCallbackUrl() != null) {
      Validators.requireHttpsCallbackUrl(params.getCallbackUrl(), "callbackUrl");
      builder.multipart(
          Collections.singletonList(Part.text("callback_url", params.getCallbackUrl())));
    }
    // No override: no body at all. OkHttp requires a RequestBody on POST, so the transport sends
    // a zero-length body with no Content-Type header.
    return transport.execute(builder.build(), ReplayCallbackResponse.class);
  }
}
