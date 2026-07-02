package com.smileidentity.generated.models;

import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/authentication (spec §6.6). {@code userId} is required and
 * travels in the multipart body, not the User-ID header. Images are required unless {@code
 * useEnrolledImage} is true.
 */
public final class AuthenticationParams {

  private final String userId;
  private final BinaryInput selfieImage;
  private final List<BinaryInput> livenessImages;
  private final Consent consent;
  private final UserDetails userDetails;
  private final Boolean useEnrolledImage;
  private final String callbackUrl;
  private final Number sandboxResult;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;

  private AuthenticationParams(Builder b) {
    this.userId = b.userId;
    this.selfieImage = b.selfieImage;
    this.livenessImages = b.livenessImages;
    this.consent = b.consent;
    this.userDetails = b.userDetails;
    this.useEnrolledImage = b.useEnrolledImage;
    this.callbackUrl = b.callbackUrl;
    this.sandboxResult = b.sandboxResult;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getUserId() {
    return userId;
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

  public Boolean getUseEnrolledImage() {
    return useEnrolledImage;
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

  public static final class Builder {
    private String userId;
    private BinaryInput selfieImage;
    private List<BinaryInput> livenessImages;
    private Consent consent;
    private UserDetails userDetails;
    private Boolean useEnrolledImage;
    private String callbackUrl;
    private Number sandboxResult;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;

    /** Required: must match an enrolled user (spec §6.6). Sent as a body field. */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

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

    /** When true, the enrolled image is used and selfie/liveness images are skipped. */
    public Builder useEnrolledImage(Boolean useEnrolledImage) {
      this.useEnrolledImage = useEnrolledImage;
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

    public AuthenticationParams build() {
      return new AuthenticationParams(this);
    }
  }
}
