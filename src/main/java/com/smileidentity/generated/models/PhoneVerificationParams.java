package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/phone. Kenya requires {@code idNumber}; Ghana requires {@code
 * operator}.
 */
public final class PhoneVerificationParams {

  private final String country;
  private final String phoneNumber;
  private final String operator;
  private final String idNumber;
  private final UserDetails userDetails;
  private final Consent consent;
  private final String userId;
  private final String callbackUrl;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;

  private PhoneVerificationParams(Builder b) {
    this.country = b.country;
    this.phoneNumber = b.phoneNumber;
    this.operator = b.operator;
    this.idNumber = b.idNumber;
    this.userDetails = b.userDetails;
    this.consent = b.consent;
    this.userId = b.userId;
    this.callbackUrl = b.callbackUrl;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getCountry() {
    return country;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getOperator() {
    return operator;
  }

  public String getIdNumber() {
    return idNumber;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public Consent getConsent() {
    return consent;
  }

  public String getUserId() {
    return userId;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public Map<String, String> getPartnerParams() {
    return partnerParams;
  }

  public List<MetadataEntry> getMetadata() {
    return metadata;
  }

  public static final class Builder {
    private String country;
    private String phoneNumber;
    private String operator;
    private String idNumber;
    private UserDetails userDetails;
    private Consent consent;
    private String userId;
    private String callbackUrl;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    /** The number to verify, in E.164 format. */
    public Builder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    /** "MTN", "VODAFONE" or "AIRTELTIGO". */
    public Builder operator(String operator) {
      this.operator = operator;
      return this;
    }

    public Builder idNumber(String idNumber) {
      this.idNumber = idNumber;
      return this;
    }

    public Builder userDetails(UserDetails userDetails) {
      this.userDetails = userDetails;
      return this;
    }

    public Builder consent(Consent consent) {
      this.consent = consent;
      return this;
    }

    /**
     * Your own identifier for the user. The API mints one when omitted; the token is bound to it
     * when set.
     */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public Builder partnerParams(Map<String, String> partnerParams) {
      this.partnerParams = partnerParams;
      return this;
    }

    public Builder metadata(List<MetadataEntry> metadata) {
      this.metadata = metadata;
      return this;
    }

    public PhoneVerificationParams build() {
      return new PhoneVerificationParams(this);
    }
  }
}
