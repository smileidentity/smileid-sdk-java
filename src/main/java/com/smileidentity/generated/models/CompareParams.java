package com.smileidentity.generated.models;

import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/compare (spec §6.7). {@code userId} is optional and travels in
 * the multipart body; when set and the comparison passes, the user is enrolled.
 */
public final class CompareParams {

  private final BinaryInput selfieImage;
  private final BinaryInput comparisonImage;
  private final ComparisonImageType comparisonImageType;
  private final Consent consent;
  private final UserDetails userDetails;
  private final List<BinaryInput> livenessImages;
  private final Boolean allowNewEnroll;
  private final String userId;
  private final String callbackUrl;
  private final Number sandboxResult;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;

  private CompareParams(Builder b) {
    this.selfieImage = b.selfieImage;
    this.comparisonImage = b.comparisonImage;
    this.comparisonImageType = b.comparisonImageType;
    this.consent = b.consent;
    this.userDetails = b.userDetails;
    this.livenessImages = b.livenessImages;
    this.allowNewEnroll = b.allowNewEnroll;
    this.userId = b.userId;
    this.callbackUrl = b.callbackUrl;
    this.sandboxResult = b.sandboxResult;
    this.partnerParams = b.partnerParams;
    this.metadata = b.metadata;
  }

  public static Builder builder() {
    return new Builder();
  }

  public BinaryInput getSelfieImage() {
    return selfieImage;
  }

  public BinaryInput getComparisonImage() {
    return comparisonImage;
  }

  public ComparisonImageType getComparisonImageType() {
    return comparisonImageType;
  }

  public Consent getConsent() {
    return consent;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public List<BinaryInput> getLivenessImages() {
    return livenessImages;
  }

  public Boolean getAllowNewEnroll() {
    return allowNewEnroll;
  }

  public String getUserId() {
    return userId;
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
    private BinaryInput selfieImage;
    private BinaryInput comparisonImage;
    private ComparisonImageType comparisonImageType;
    private Consent consent;
    private UserDetails userDetails;
    private List<BinaryInput> livenessImages;
    private Boolean allowNewEnroll;
    private String userId;
    private String callbackUrl;
    private Number sandboxResult;
    private Map<String, String> partnerParams;
    private List<MetadataEntry> metadata;

    public Builder selfieImage(BinaryInput selfieImage) {
      this.selfieImage = selfieImage;
      return this;
    }

    public Builder comparisonImage(BinaryInput comparisonImage) {
      this.comparisonImage = comparisonImage;
      return this;
    }

    public Builder comparisonImageType(ComparisonImageType comparisonImageType) {
      this.comparisonImageType = comparisonImageType;
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

    /** Optional: active liveness is skipped when omitted (spec §6.7). */
    public Builder livenessImages(List<BinaryInput> livenessImages) {
      this.livenessImages = livenessImages;
      return this;
    }

    public Builder allowNewEnroll(Boolean allowNewEnroll) {
      this.allowNewEnroll = allowNewEnroll;
      return this;
    }

    /** Optional: sent as a body field (spec §6.7). */
    public Builder userId(String userId) {
      this.userId = userId;
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

    public CompareParams build() {
      return new CompareParams(this);
    }
  }
}
