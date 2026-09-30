package com.smileidentity.generated.models;

import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/**
 * Request parameters for POST /v3/residency_document_verification. Same shape as enhanced document
 * verification plus the required {@code visa} image; {@code idType} must be PASSPORT.
 */
public final class ResidencyDocumentVerificationParams {

  private final BinaryInput selfieImage;
  private final List<BinaryInput> livenessImages;
  private final BinaryInput document;
  private final BinaryInput documentBack;
  private final BinaryInput visa;
  private final Consent consent;
  private final String country;
  private final String idType;
  private final UserDetails userDetails;
  private final String callbackUrl;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;
  private final String userId;

  private ResidencyDocumentVerificationParams(Builder b) {
    this.selfieImage = b.selfieImage;
    this.livenessImages = b.livenessImages;
    this.document = b.document;
    this.documentBack = b.documentBack;
    this.visa = b.visa;
    this.consent = b.consent;
    this.country = b.country;
    this.idType = b.idType;
    this.userDetails = b.userDetails;
    this.callbackUrl = b.callbackUrl;
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

  public BinaryInput getDocument() {
    return document;
  }

  public BinaryInput getDocumentBack() {
    return documentBack;
  }

  public BinaryInput getVisa() {
    return visa;
  }

  public Consent getConsent() {
    return consent;
  }

  public String getCountry() {
    return country;
  }

  public String getIdType() {
    return idType;
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

  public String getUserId() {
    return userId;
  }

  public static final class Builder {
    private BinaryInput selfieImage;
    private List<BinaryInput> livenessImages;
    private BinaryInput document;
    private BinaryInput documentBack;
    private BinaryInput visa;
    private Consent consent;
    private String country;
    private String idType;
    private UserDetails userDetails;
    private String callbackUrl;
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

    public Builder document(BinaryInput document) {
      this.document = document;
      return this;
    }

    public Builder documentBack(BinaryInput documentBack) {
      this.documentBack = documentBack;
      return this;
    }

    /** The visa endorsed in the passport (JPEG or PNG). Required. */
    public Builder visa(BinaryInput visa) {
      this.visa = visa;
      return this;
    }

    public Builder consent(Consent consent) {
      this.consent = consent;
      return this;
    }

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    /** Optional; PASSPORT is sent when null or empty, and any other value is rejected. */
    public Builder idType(String idType) {
      this.idType = idType;
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

    /** Sent as the User-ID header . */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public ResidencyDocumentVerificationParams build() {
      return new ResidencyDocumentVerificationParams(this);
    }
  }
}
