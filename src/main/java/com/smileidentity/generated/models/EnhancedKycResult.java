package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of an Enhanced KYC verification: the body of the POST /v3/enhanced_kyc/sync 200 response,
 * which is the same payload the async job POSTs to the callback URL. {@code status} is "clear",
 * "attention", "block" or "error".
 *
 * <p>The spec leaves the payload open, so only the fields below are typed. Every other field is
 * kept, as parsed JSON, in {@link #getAdditionalProperties()}.
 */
public final class EnhancedKycResult {

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("kyc_receipt")
  private String kycReceipt;

  @JsonProperty("response_signature")
  private String responseSignature;

  @JsonProperty("response_timestamp")
  private String responseTimestamp;

  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  /**
   * The Smile job id for this verification. On the POST /v3/enhanced_kyc/sync result it comes from
   * the {@code SmileID-Job-ID} response header, falling back to the body's {@code job_id} when the
   * header is absent.
   */
  public String getJobId() {
    return jobId;
  }

  /**
   * The partner-facing user id. On the POST /v3/enhanced_kyc/sync result it comes from the {@code
   * User-ID} response header, falling back to the body's {@code user_id} when the header is absent.
   */
  public String getUserId() {
    return userId;
  }

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  /** Why the job landed where it did; null for a clear outcome. */
  public String getReason() {
    return reason;
  }

  public String getKycReceipt() {
    return kycReceipt;
  }

  /**
   * The {@code Response-Signature} header of the POST /v3/enhanced_kyc/sync response, or null when
   * it was absent. It is the same signature a webhook carries, so you can check it with your own
   * webhook verification code. The SDK does not verify it.
   */
  public String getResponseSignature() {
    return responseSignature;
  }

  /**
   * The {@code Response-Timestamp} header of the POST /v3/enhanced_kyc/sync response, the timestamp
   * the signature was computed against, or null when it was absent.
   */
  public String getResponseTimestamp() {
    return responseTimestamp;
  }

  /** Every other field of the payload, keyed by its wire name. Never null; read-only. */
  public Map<String, Object> getAdditionalProperties() {
    return Collections.unmodifiableMap(additionalProperties);
  }

  @JsonAnySetter
  private void setAdditionalProperty(String name, Object value) {
    additionalProperties.put(name, value);
  }
}
