package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Terminal result of a One-Time AML screening: the body of the POST /v3/aml/sync 200 response, and
 * the payload the async callback delivers. {@code status} is "clear", "block", "attention" or
 * "error".
 */
public final class OneTimeAmlCallback {

  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("completed_at")
  private String completedAt;

  @JsonProperty("product")
  private String product;

  @JsonProperty("partner_params")
  private Map<String, String> partnerParams;

  @JsonProperty("kyc_receipt")
  private String kycReceipt;

  @JsonProperty("strict_match")
  private Boolean strictMatch;

  @JsonProperty("no_of_persons_found")
  private Integer noOfPersonsFound;

  @JsonProperty("people")
  private List<AmlPersonMatch> people;

  @JsonProperty("user_provided_info")
  private UserProvidedInfo userProvidedInfo;

  @JsonProperty("consent")
  private Map<String, Object> consent;

  @JsonProperty("antifraud")
  private Map<String, Object> antifraud;

  @JsonProperty("device_signals")
  private Map<String, Object> deviceSignals;

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  /** Machine-readable reason; null for a clear outcome. */
  public String getReason() {
    return reason;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getCompletedAt() {
    return completedAt;
  }

  public String getProduct() {
    return product;
  }

  public Map<String, String> getPartnerParams() {
    return partnerParams;
  }

  public String getKycReceipt() {
    return kycReceipt;
  }

  public Boolean getStrictMatch() {
    return strictMatch;
  }

  public Integer getNoOfPersonsFound() {
    return noOfPersonsFound;
  }

  public List<AmlPersonMatch> getPeople() {
    return people;
  }

  public UserProvidedInfo getUserProvidedInfo() {
    return userProvidedInfo;
  }

  public Map<String, Object> getConsent() {
    return consent;
  }

  public Map<String, Object> getAntifraud() {
    return antifraud;
  }

  public Map<String, Object> getDeviceSignals() {
    return deviceSignals;
  }

  /** The user details as the partner supplied them. */
  public static final class UserProvidedInfo {

    @JsonProperty("given_names")
    private String givenNames;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    public String getGivenNames() {
      return givenNames;
    }

    public String getLastName() {
      return lastName;
    }

    public String getEmail() {
      return email;
    }

    public String getPhoneNumber() {
      return phoneNumber;
    }
  }
}
