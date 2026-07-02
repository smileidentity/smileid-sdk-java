package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/** Request parameters for POST /v3/enhanced_kyc (spec §6.1). */
public final class EnhancedKycParams {

  private final String country;
  private final String idType;
  private final String idNumber;
  private final UserDetails userDetails;
  private final Consent consent;
  private final String callbackUrl;
  private final String bankCode;
  private final String operator;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;
  private final String userId;

  private EnhancedKycParams(Builder b) {
    this.country = b.country;
    this.idType = b.idType;
    this.idNumber = b.idNumber;
    this.userDetails = b.userDetails;
    this.consent = b.consent;
    this.callbackUrl = b.callbackUrl;
    this.bankCode = b.bankCode;
    this.operator = b.operator;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
    this.userId = b.userId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getCountry() {
    return country;
  }

  public String getIdType() {
    return idType;
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

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public String getBankCode() {
    return bankCode;
  }

  public String getOperator() {
    return operator;
  }

  public Map<String, String> getPartnerParams() {
    return partnerParams;
  }

  public List<MetadataEntry> getMetadata() {
    return metadata;
  }

  public String getUserId() {
    return userId;
  }

  public static final class Builder {
    private String country;
    private String idType;
    private String idNumber;
    private UserDetails userDetails;
    private Consent consent;
    private String callbackUrl;
    private String bankCode;
    private String operator;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;
    private String userId;

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    public Builder idType(String idType) {
      this.idType = idType;
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

    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public Builder bankCode(String bankCode) {
      this.bankCode = bankCode;
      return this;
    }

    public Builder operator(String operator) {
      this.operator = operator;
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

    /** Sent as the User-ID header (spec §6.1). */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public EnhancedKycParams build() {
      return new EnhancedKycParams(this);
    }
  }
}
