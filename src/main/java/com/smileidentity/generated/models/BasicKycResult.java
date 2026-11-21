package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Result of a Basic KYC verification: the body of the POST /v3/basic_kyc/sync 200 response and the
 * payload the async callback delivers. {@code status} is "clear", "attention", "block" or "error".
 * Basic KYC reports whether the submitted details match the ID authority's record; it never returns
 * the record.
 */
public final class BasicKycResult {

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
  private Map<String, Object> partnerParams;

  @JsonProperty("match_results")
  private MatchResults matchResults;

  @JsonProperty("user_provided_info")
  private UserProvidedInfo userProvidedInfo;

  @JsonProperty("consent")
  private Map<String, Object> consent;

  @JsonProperty("antifraud")
  private Map<String, Object> antifraud;

  @JsonProperty("device_signals")
  private Map<String, Object> deviceSignals;

  @JsonProperty("kyc_receipt")
  private String kycReceipt;

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

  public String getCreatedAt() {
    return createdAt;
  }

  /** Absent while a job is still running. */
  public String getCompletedAt() {
    return completedAt;
  }

  /** Always "basic_kyc". */
  public String getProduct() {
    return product;
  }

  public Map<String, Object> getPartnerParams() {
    return partnerParams;
  }

  /** The verdict per submitted field; null when the check never produced one. */
  public MatchResults getMatchResults() {
    return matchResults;
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

  public String getKycReceipt() {
    return kycReceipt;
  }

  /**
   * The verdict per submitted field. Each verdict is one of "exact_match", "partial_match",
   * "transposed", "no_match" or "not_provided".
   */
  public static final class MatchResults {

    @JsonProperty("overall")
    private String overall;

    @JsonProperty("names")
    private String names;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("other_names")
    private String otherNames;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("secondary_id_number")
    private String secondaryIdNumber;

    /** The verdict across every field that could be compared; it drives {@code status}. */
    public String getOverall() {
      return overall;
    }

    public String getNames() {
      return names;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getOtherNames() {
      return otherNames;
    }

    public String getDateOfBirth() {
      return dateOfBirth;
    }

    public String getGender() {
      return gender;
    }

    public String getPhoneNumber() {
      return phoneNumber;
    }

    public String getSecondaryIdNumber() {
      return secondaryIdNumber;
    }
  }

  /** The identity details submitted with the request, echoed back. */
  public static final class UserProvidedInfo {

    @JsonProperty("given_names")
    private String givenNames;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("id_number")
    private String idNumber;

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

    public String getIdNumber() {
      return idNumber;
    }
  }
}
