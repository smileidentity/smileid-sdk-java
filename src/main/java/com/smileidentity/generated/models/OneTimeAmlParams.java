package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/** Request parameters for POST /v3/aml and POST /v3/aml/sync (One-Time AML). */
public final class OneTimeAmlParams {

  private final String userId;
  private final List<String> countries;
  private final String fullName;
  private final String birthYear;
  private final Boolean strictMatch;
  private final List<String> aliases;
  private final Boolean searchExistingUser;
  private final Consent consent;
  private final UserDetails userDetails;
  private final String callbackUrl;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;

  private OneTimeAmlParams(Builder b) {
    this.userId = b.userId;
    this.countries = b.countries;
    this.fullName = b.fullName;
    this.birthYear = b.birthYear;
    this.strictMatch = b.strictMatch;
    this.aliases = b.aliases;
    this.searchExistingUser = b.searchExistingUser;
    this.consent = b.consent;
    this.userDetails = b.userDetails;
    this.callbackUrl = b.callbackUrl;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getUserId() {
    return userId;
  }

  public List<String> getCountries() {
    return countries;
  }

  public String getFullName() {
    return fullName;
  }

  public String getBirthYear() {
    return birthYear;
  }

  public Boolean getStrictMatch() {
    return strictMatch;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public Boolean getSearchExistingUser() {
    return searchExistingUser;
  }

  public Consent getConsent() {
    return consent;
  }

  public UserDetails getUserDetails() {
    return userDetails;
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
    private String userId;
    private List<String> countries;
    private String fullName;
    private String birthYear;
    private Boolean strictMatch;
    private List<String> aliases;
    private Boolean searchExistingUser;
    private Consent consent;
    private UserDetails userDetails;
    private String callbackUrl;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;

    /** Sent as the {@code user_id} form field; required when searchExistingUser is true. */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    /** ISO 3166-1 alpha-2 country codes to screen against; sent as a JSON array string. */
    public Builder countries(List<String> countries) {
      this.countries = countries;
      return this;
    }

    /** Full name to screen (required). */
    public Builder fullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    /** Optional four-digit birth year. */
    public Builder birthYear(String birthYear) {
      this.birthYear = birthYear;
      return this;
    }

    /** Defaults to true on the server when omitted. */
    public Builder strictMatch(Boolean strictMatch) {
      this.strictMatch = strictMatch;
      return this;
    }

    /** At most 20 alternate names; sent as a JSON array string. */
    public Builder aliases(List<String> aliases) {
      this.aliases = aliases;
      return this;
    }

    public Builder searchExistingUser(Boolean searchExistingUser) {
      this.searchExistingUser = searchExistingUser;
      return this;
    }

    public Builder consent(Consent consent) {
      this.consent = consent;
      return this;
    }

    public Builder userDetails(UserDetails userDetails) {
      this.userDetails = userDetails;
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

    public OneTimeAmlParams build() {
      return new OneTimeAmlParams(this);
    }
  }
}
