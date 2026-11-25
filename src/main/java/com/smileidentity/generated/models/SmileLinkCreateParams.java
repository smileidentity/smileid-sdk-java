package com.smileidentity.generated.models;

import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/smile_links. {@code idTypes}, {@code name}, {@code
 * noticePrivacyPolicyUrl} and {@code isSingleUse} are required.
 */
public final class SmileLinkCreateParams {

  private final List<SmileLinkIdType> idTypes;
  private final String name;
  private final String companyName;
  private final String noticePrivacyPolicyUrl;
  private final Boolean isSingleUse;
  private final String callbackUrl;
  private final String description;
  private final String expiresAt;
  private final String logoUrl;
  private final String redirectUrl;
  private final String themeColor;
  private final Map<String, String> partnerParams;
  private final String userId;

  private SmileLinkCreateParams(Builder b) {
    this.idTypes = b.idTypes;
    this.name = b.name;
    this.companyName = b.companyName;
    this.noticePrivacyPolicyUrl = b.noticePrivacyPolicyUrl;
    this.isSingleUse = b.isSingleUse;
    this.callbackUrl = b.callbackUrl;
    this.description = b.description;
    this.expiresAt = b.expiresAt;
    this.logoUrl = b.logoUrl;
    this.redirectUrl = b.redirectUrl;
    this.themeColor = b.themeColor;
    this.partnerParams = b.partnerParams;
    this.userId = b.userId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<SmileLinkIdType> getIdTypes() {
    return idTypes;
  }

  public String getName() {
    return name;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getNoticePrivacyPolicyUrl() {
    return noticePrivacyPolicyUrl;
  }

  public Boolean getIsSingleUse() {
    return isSingleUse;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public String getDescription() {
    return description;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public String getThemeColor() {
    return themeColor;
  }

  public Map<String, String> getPartnerParams() {
    return partnerParams;
  }

  public String getUserId() {
    return userId;
  }

  public static final class Builder {
    private List<SmileLinkIdType> idTypes;
    private String name;
    private String companyName;
    private String noticePrivacyPolicyUrl;
    private Boolean isSingleUse;
    private String callbackUrl;
    private String description;
    private String expiresAt;
    private String logoUrl;
    private String redirectUrl;
    private String themeColor;
    private Map<String, String> partnerParams;
    private String userId;

    public Builder idTypes(List<SmileLinkIdType> idTypes) {
      this.idTypes = idTypes;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder companyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    public Builder noticePrivacyPolicyUrl(String noticePrivacyPolicyUrl) {
      this.noticePrivacyPolicyUrl = noticePrivacyPolicyUrl;
      return this;
    }

    public Builder isSingleUse(Boolean isSingleUse) {
      this.isSingleUse = isSingleUse;
      return this;
    }

    /**
     * Where job results are delivered. Defaults to the client's default callback URL, then to the
     * partner's configured callback.
     */
    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /** ISO 8601 date-time. The API defaults it to 90 days from creation for single-use links. */
    public Builder expiresAt(String expiresAt) {
      this.expiresAt = expiresAt;
      return this;
    }

    public Builder logoUrl(String logoUrl) {
      this.logoUrl = logoUrl;
      return this;
    }

    public Builder redirectUrl(String redirectUrl) {
      this.redirectUrl = redirectUrl;
      return this;
    }

    public Builder themeColor(String themeColor) {
      this.themeColor = themeColor;
      return this;
    }

    public Builder partnerParams(Map<String, String> partnerParams) {
      this.partnerParams = partnerParams;
      return this;
    }

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public SmileLinkCreateParams build() {
      return new SmileLinkCreateParams(this);
    }
  }
}
