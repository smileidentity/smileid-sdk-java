package com.smileidentity.generated.models;

/** Query parameters for GET /v3/services/supported_documents (spec §6.14). All optional. */
public final class SupportedDocumentsParams {

  private final String continent;
  private final String countryCode;
  private final String locale;

  private SupportedDocumentsParams(Builder b) {
    this.continent = b.continent;
    this.countryCode = b.countryCode;
    this.locale = b.locale;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getContinent() {
    return continent;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getLocale() {
    return locale;
  }

  public static final class Builder {
    private String continent;
    private String countryCode;
    private String locale;

    /** One of AFRICA, ASIA, EUROPE, NORTH AMERICA, OCEANIA, SOUTH AMERICA. */
    public Builder continent(String continent) {
      this.continent = continent;
      return this;
    }

    /** ISO 3166-1 alpha-2 country code. */
    public Builder countryCode(String countryCode) {
      this.countryCode = countryCode;
      return this;
    }

    /** One of en-GB, fr-FR, ar-EG (default en-GB server-side). */
    public Builder locale(String locale) {
      this.locale = locale;
      return this;
    }

    public SupportedDocumentsParams build() {
      return new SupportedDocumentsParams(this);
    }
  }
}
