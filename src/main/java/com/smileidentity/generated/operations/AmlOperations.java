package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AmlMonitoringNewsParams;
import com.smileidentity.generated.models.AmlMonitoringNewsResponse;
import com.smileidentity.generated.models.AmlMonitoringParams;
import com.smileidentity.generated.models.OneTimeAmlAcceptedResponse;
import com.smileidentity.generated.models.OneTimeAmlCallback;
import com.smileidentity.generated.models.OneTimeAmlParams;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin operation functions for the AML endpoints: POST /v3/aml, POST /v3/aml/sync, POST
 * /v3/aml/monitoring and GET /v3/aml/monitoring/{job_id}/news/{reference}.
 */
public final class AmlOperations {

  private AmlOperations() {}

  public static OneTimeAmlAcceptedResponse oneTime(
      Transport transport, OneTimeAmlParams params, RequestOptions options) {
    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/aml")
            .authenticated(true)
            .multipart(oneTimeParts(transport, params, options))
            .options(options)
            .build();
    return transport.execute(request, OneTimeAmlAcceptedResponse.class);
  }

  /**
   * Retryable only when an idempotency key is sent: a retry then resolves to the same job instead
   * of starting a second, separately billed screening.
   */
  public static OneTimeAmlCallback oneTimeSync(
      Transport transport, OneTimeAmlParams params, String idempotencyKey, RequestOptions options) {
    ApiRequest.Builder builder =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/aml/sync")
            .authenticated(true)
            .multipart(oneTimeParts(transport, params, options))
            .idempotent(idempotencyKey != null)
            .options(options);
    if (idempotencyKey != null) {
      builder.header("SmileID-Idempotency-Key", idempotencyKey);
    }
    return transport.execute(builder.build(), OneTimeAmlCallback.class);
  }

  public static AcceptedResponse monitoring(
      Transport transport, AmlMonitoringParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "user_id", params.getUserId());
    OperationSupport.addJson(parts, "countries", params.getCountries());
    OperationSupport.addText(parts, "full_name", params.getFullName());
    OperationSupport.addText(parts, "birth_year", params.getBirthYear());
    OperationSupport.addBoolean(parts, "strict_match", params.getStrictMatch());
    OperationSupport.addJson(parts, "aliases", params.getAliases());
    OperationSupport.addBoolean(parts, "search_existing_user", params.getSearchExistingUser());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());
    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/aml/monitoring")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }

  public static AmlMonitoringNewsResponse monitoringNews(
      Transport transport,
      String jobId,
      String reference,
      AmlMonitoringNewsParams params,
      RequestOptions options) {
    ApiRequest.Builder builder =
        ApiRequest.builder()
            .method("GET")
            .path(
                "/v3/aml/monitoring/"
                    + OperationSupport.encodePathSegment(jobId)
                    + "/news/"
                    + OperationSupport.encodePathSegment(reference))
            .authenticated(true)
            .idempotent(true)
            .options(options)
            .query("news_category", params.getNewsCategory());
    if (params.getPage() != null) {
      builder.query("page", String.valueOf(params.getPage()));
    }
    if (params.getLimit() != null) {
      builder.query("limit", String.valueOf(params.getLimit()));
    }
    return transport.execute(builder.build(), AmlMonitoringNewsResponse.class);
  }

  private static List<Part> oneTimeParts(
      Transport transport, OneTimeAmlParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "user_id", params.getUserId());
    OperationSupport.addJson(parts, "countries", params.getCountries());
    OperationSupport.addText(parts, "full_name", params.getFullName());
    OperationSupport.addText(parts, "birth_year", params.getBirthYear());
    OperationSupport.addBoolean(parts, "strict_match", params.getStrictMatch());
    OperationSupport.addJson(parts, "aliases", params.getAliases());
    OperationSupport.addBoolean(parts, "search_existing_user", params.getSearchExistingUser());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());
    return parts;
  }
}
