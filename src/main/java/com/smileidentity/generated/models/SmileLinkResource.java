package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * A Smile Link: the 200 response of GET and PUT /v3/smile_links/{refId}. Date-times are ISO 8601
 * strings.
 */
public class SmileLinkResource {

  @JsonProperty("ref_id")
  private String refId;

  @JsonProperty("link")
  private String link;

  @JsonProperty("name")
  private String name;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("id_types")
  private List<SmileLinkIdType> idTypes;

  @JsonProperty("is_single_use")
  private Boolean isSingleUse;

  @JsonProperty("callback_url")
  private String callbackUrl;

  @JsonProperty("notice_privacy_policy_url")
  private String noticePrivacyPolicyUrl;

  @JsonProperty("logo_url")
  private String logoUrl;

  @JsonProperty("redirect_url")
  private String redirectUrl;

  @JsonProperty("theme_color")
  private String themeColor;

  @JsonProperty("expires_at")
  private String expiresAt;

  @JsonProperty("disabled_at")
  private String disabledAt;

  @JsonProperty("partner_params")
  private Map<String, Object> partnerParams;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("token_provided_fields")
  private List<String> tokenProvidedFields;

  @JsonProperty("token_provided_values")
  private Map<String, Object> tokenProvidedValues;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  /** The link's id, used on every smile_links call. */
  public String getRefId() {
    return refId;
  }

  /** The URL to hand to the end user. */
  public String getLink() {
    return link;
  }

  public String getName() {
    return name;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getDescription() {
    return description;
  }

  public List<SmileLinkIdType> getIdTypes() {
    return idTypes;
  }

  public Boolean getIsSingleUse() {
    return isSingleUse;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public String getNoticePrivacyPolicyUrl() {
    return noticePrivacyPolicyUrl;
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

  public String getExpiresAt() {
    return expiresAt;
  }

  /** Set while the link is disabled; null when it is active. */
  public String getDisabledAt() {
    return disabledAt;
  }

  public Map<String, Object> getPartnerParams() {
    return partnerParams;
  }

  /** The user id a single-use link's job runs under; null on a reusable link. */
  public String getUserId() {
    return userId;
  }

  /** Field names bound onto the link from the creating token's payload; empty when none. */
  public List<String> getTokenProvidedFields() {
    return tokenProvidedFields;
  }

  /** Values for the bound fields held in the clear (country, id_type, consent). */
  public Map<String, Object> getTokenProvidedValues() {
    return tokenProvidedValues;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
