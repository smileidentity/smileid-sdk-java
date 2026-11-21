package com.smileidentity.client;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AmlMonitoringNewsParams;
import com.smileidentity.generated.models.AmlMonitoringNewsResponse;
import com.smileidentity.generated.models.AmlMonitoringParams;
import com.smileidentity.generated.models.OneTimeAmlAcceptedResponse;
import com.smileidentity.generated.models.OneTimeAmlCallback;
import com.smileidentity.generated.models.OneTimeAmlParams;
import com.smileidentity.generated.operations.AmlOperations;
import com.smileidentity.helpers.Validators;
import java.util.regex.Pattern;

/**
 * {@code client.aml()} — One-Time AML (POST /v3/aml, POST /v3/aml/sync) and AML Monitoring (POST
 * /v3/aml/monitoring, GET /v3/aml/monitoring/{job_id}/news/{reference}).
 */
public final class AmlResource {

  private static final Pattern IDEMPOTENCY_KEY = Pattern.compile("[A-Za-z0-9_-]{16,128}");

  private final Transport transport;

  AmlResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/aml — queues a screening; the result is delivered to the callback URL. */
  public OneTimeAmlAcceptedResponse oneTime(OneTimeAmlParams params) {
    return oneTime(params, RequestOptions.none());
  }

  public OneTimeAmlAcceptedResponse oneTime(OneTimeAmlParams params, RequestOptions options) {
    validate(params);
    return AmlOperations.oneTime(transport, params, options);
  }

  /**
   * POST /v3/aml/sync — screens inline and returns the terminal result, with the job id from the
   * {@code SmileID-Job-ID} header. Slower than {@link #oneTime}. A 408 means the provider did not
   * answer in time, nothing was billed and the job is final in error: resubmit rather than poll. On
   * a 500 with a job id ({@code e.getJobId()}), read the outcome with {@code
   * verifications().retrieve}.
   */
  public OneTimeAmlCallback oneTimeSync(OneTimeAmlParams params) {
    return oneTimeSync(params, null, RequestOptions.none());
  }

  /**
   * As {@link #oneTimeSync(OneTimeAmlParams)}, sending {@code idempotencyKey} (16 to 128 characters
   * of {@code A-Z a-z 0-9 - _}) as the {@code SmileID-Idempotency-Key} header. A request carrying a
   * key that was already used returns the original result instead of screening again, so such
   * requests are retried like other idempotent calls. Pass null to send no key.
   */
  public OneTimeAmlCallback oneTimeSync(
      OneTimeAmlParams params, String idempotencyKey, RequestOptions options) {
    validate(params);
    if (idempotencyKey != null && !IDEMPOTENCY_KEY.matcher(idempotencyKey).matches()) {
      throw new ValidationException(
          "idempotency_key must be 16 to 128 characters of A-Z, a-z, 0-9, - and _");
    }
    return AmlOperations.oneTimeSync(transport, params, idempotencyKey, options);
  }

  /** POST /v3/aml/monitoring — enrols the entity for ongoing monitoring. */
  public AcceptedResponse monitoring(AmlMonitoringParams params) {
    return monitoring(params, RequestOptions.none());
  }

  public AcceptedResponse monitoring(AmlMonitoringParams params, RequestOptions options) {
    requireCountries(params.getCountries() == null || params.getCountries().isEmpty());
    Validators.requireEmailOrPhone(params.getUserDetails());
    requireConsent(params.getConsent() == null);
    if (!Boolean.TRUE.equals(params.getSearchExistingUser()) && isBlank(params.getFullName())) {
      throw new ValidationException("full_name is required unless search_existing_user is true");
    }
    requireUserIdWhenSearching(params.getSearchExistingUser(), params.getUserId());
    return AmlOperations.monitoring(transport, params, options);
  }

  /** GET /v3/aml/monitoring/{job_id}/news/{reference}. */
  public AmlMonitoringNewsResponse monitoringNews(
      String jobId, String reference, AmlMonitoringNewsParams params) {
    return monitoringNews(jobId, reference, params, RequestOptions.none());
  }

  public AmlMonitoringNewsResponse monitoringNews(
      String jobId, String reference, AmlMonitoringNewsParams params, RequestOptions options) {
    if (isBlank(jobId)) {
      throw new ValidationException("job_id is required");
    }
    if (isBlank(reference)) {
      throw new ValidationException("reference is required");
    }
    if (params == null || isBlank(params.getNewsCategory())) {
      throw new ValidationException("news_category is required");
    }
    return AmlOperations.monitoringNews(transport, jobId, reference, params, options);
  }

  private static void validate(OneTimeAmlParams params) {
    requireCountries(params.getCountries() == null || params.getCountries().isEmpty());
    if (isBlank(params.getFullName())) {
      throw new ValidationException("full_name is required");
    }
    Validators.requireEmailOrPhone(params.getUserDetails());
    requireConsent(params.getConsent() == null);
    requireUserIdWhenSearching(params.getSearchExistingUser(), params.getUserId());
  }

  /** The spec requires user_id in the body when search_existing_user is true. */
  private static void requireUserIdWhenSearching(Boolean searchExistingUser, String userId) {
    if (Boolean.TRUE.equals(searchExistingUser) && isBlank(userId)) {
      throw new ValidationException("user_id is required when search_existing_user is true");
    }
  }

  private static void requireCountries(boolean missing) {
    if (missing) {
      throw new ValidationException("countries must contain at least one country code");
    }
  }

  private static void requireConsent(boolean missing) {
    if (missing) {
      throw new ValidationException("consent is required");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
