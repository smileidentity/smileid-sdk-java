package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/** Request parameters for POST /v3/basic_kyc and POST /v3/basic_kyc/sync. */
public final class BasicKycParams {

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

  private BasicKycParams(Builder b) {
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

    /**
     * Where {@code verify} delivers the result. {@code verifySync} sends it only when set here,
     * never the client's default, because the sync endpoint ignores it.
     */
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

    public BasicKycParams build() {
      return new BasicKycParams(this);
    }
  }
}
