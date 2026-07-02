package com.smileidentity.generated.models;

import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/** Request parameters for POST /v3/registration — biometric enrollment (spec §6.5). */
public final class EnrollParams {

  private final BinaryInput selfieImage;
  private final List<BinaryInput> livenessImages;
  private final Consent consent;
  private final UserDetails userDetails;
  private final Boolean allowNewEnroll;
  private final String callbackUrl;
  private final Number sandboxResult;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;
  private final String userId;

  private EnrollParams(Builder b) {
    this.selfieImage = b.selfieImage;
    this.livenessImages = b.livenessImages;
    this.consent = b.consent;
    this.userDetails = b.userDetails;
    this.allowNewEnroll = b.allowNewEnroll;
    this.callbackUrl = b.callbackUrl;
    this.sandboxResult = b.sandboxResult;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
    this.userId = b.userId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public BinaryInput getSelfieImage() {
    return selfieImage;
  }

  public List<BinaryInput> getLivenessImages() {
    return livenessImages;
  }

  public Consent getConsent() {
    return consent;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public Boolean getAllowNewEnroll() {
    return allowNewEnroll;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public Number getSandboxResult() {
    return sandboxResult;
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
    private BinaryInput selfieImage;
    private List<BinaryInput> livenessImages;
    private Consent consent;
    private UserDetails userDetails;
    private Boolean allowNewEnroll;
    private String callbackUrl;
    private Number sandboxResult;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;
    private String userId;

    public Builder selfieImage(BinaryInput selfieImage) {
      this.selfieImage = selfieImage;
      return this;
    }

    public Builder livenessImages(List<BinaryInput> livenessImages) {
      this.livenessImages = livenessImages;
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

    public Builder allowNewEnroll(Boolean allowNewEnroll) {
      this.allowNewEnroll = allowNewEnroll;
      return this;
    }

    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    public Builder sandboxResult(Number sandboxResult) {
      this.sandboxResult = sandboxResult;
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

    /** Sent as the User-ID header (spec §6.5). */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public EnrollParams build() {
      return new EnrollParams(this);
    }
  }
}
