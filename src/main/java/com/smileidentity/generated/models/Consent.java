package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * End-user consent, required on all entry endpoints (spec §5.1). Serialized as a JSON multipart
 * part named {@code consent}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"granted", "granted_at", "notice_language", "notice_privacy_policy_url"})
public final class Consent {

  private static final DateTimeFormatter ISO_MILLIS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  @JsonProperty("granted")
  private final Boolean granted;

  @JsonProperty("granted_at")
  private final String grantedAt;

  @JsonProperty("notice_language")
  private final String noticeLanguage;

  @JsonProperty("notice_privacy_policy_url")
  private final String noticePrivacyPolicyUrl;

  private Consent(Boolean granted, String grantedAt, String noticeLanguage, String noticeUrl) {
    this.granted = granted;
    this.grantedAt = grantedAt;
    this.noticeLanguage = noticeLanguage;
    this.noticePrivacyPolicyUrl = noticeUrl;
  }

  /** Builds a granted consent ({@code granted} is always true on the wire). */
  public static Consent granted(
      Instant grantedAt, String noticeLanguage, String noticePrivacyPolicyUrl) {
    return granted(ISO_MILLIS.format(grantedAt), noticeLanguage, noticePrivacyPolicyUrl);
  }

  /** Builds a granted consent from a pre-formatted ISO 8601 timestamp. */
  public static Consent granted(
      String grantedAt, String noticeLanguage, String noticePrivacyPolicyUrl) {
    return new Consent(true, grantedAt, noticeLanguage, noticePrivacyPolicyUrl);
  }

  public Boolean getGranted() {
    return granted;
  }

  public String getGrantedAt() {
    return grantedAt;
  }

  public String getNoticeLanguage() {
    return noticeLanguage;
  }

  public String getNoticePrivacyPolicyUrl() {
    return noticePrivacyPolicyUrl;
  }
}
