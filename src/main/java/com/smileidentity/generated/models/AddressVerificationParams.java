package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/address. Either {@code idNumber} or {@code utilityNumber} is
 * needed: Nigeria requires {@code utilityNumber}, {@code utilityProvider} and {@code utilityType};
 * South Africa requires {@code idNumber}.
 */
public final class AddressVerificationParams {

  private final String address;
  private final String country;
  private final String idNumber;
  private final String utilityNumber;
  private final String utilityProvider;
  private final String utilityType;
  private final UserDetails userDetails;
  private final Consent consent;
  private final String userId;
  private final String callbackUrl;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;

  private AddressVerificationParams(Builder b) {
    this.address = b.address;
    this.country = b.country;
    this.idNumber = b.idNumber;
    this.utilityNumber = b.utilityNumber;
    this.utilityProvider = b.utilityProvider;
    this.utilityType = b.utilityType;
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

  public String getAddress() {
    return address;
  }

  public String getCountry() {
    return country;
  }

  public String getIdNumber() {
    return idNumber;
  }

  public String getUtilityNumber() {
    return utilityNumber;
  }

  public String getUtilityProvider() {
    return utilityProvider;
  }

  public String getUtilityType() {
    return utilityType;
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
    private String address;
    private String country;
    private String idNumber;
    private String utilityNumber;
    private String utilityProvider;
    private String utilityType;
    private UserDetails userDetails;
    private Consent consent;
    private String userId;
    private String callbackUrl;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;

    public Builder address(String address) {
      this.address = address;
      return this;
    }

    /** "NG" or "ZA". */
    public Builder country(String country) {
      this.country = country;
      return this;
    }

    public Builder idNumber(String idNumber) {
      this.idNumber = idNumber;
      return this;
    }

    public Builder utilityNumber(String utilityNumber) {
      this.utilityNumber = utilityNumber;
      return this;
    }

    public Builder utilityProvider(String utilityProvider) {
      this.utilityProvider = utilityProvider;
      return this;
    }

    /** "PrePaid" or "PostPaid". */
    public Builder utilityType(String utilityType) {
      this.utilityType = utilityType;
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

    public AddressVerificationParams build() {
      return new AddressVerificationParams(this);
    }
  }
}
