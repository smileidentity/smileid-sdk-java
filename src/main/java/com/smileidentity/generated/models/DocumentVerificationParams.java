package com.smileidentity.generated.models;

import com.smileidentity.helpers.BinaryInput;
import java.util.List;
import java.util.Map;

/** Request parameters for POST /v3/document_verification (spec §6.2). */
public final class DocumentVerificationParams {

  private final BinaryInput selfieImage;
  private final List<BinaryInput> livenessImages;
  private final BinaryInput document;
  private final BinaryInput documentBack;
  private final Consent consent;
  private final String country;
  private final String idType;
  private final UserDetails userDetails;
  private final String callbackUrl;
  private final Map<String, String> partnerParams;
  private final List<MetadataEntry> metadata;
  private final String userId;

  private DocumentVerificationParams(Builder b) {
    this.selfieImage = b.selfieImage;
    this.livenessImages = b.livenessImages;
    this.document = b.document;
    this.documentBack = b.documentBack;
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

    public Builder consent(Consent consent) {
      this.consent = consent;
      return this;
    }

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    /** Optional: the document is auto-classified when omitted (spec §6.2). */
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

    /** Sent as the User-ID header (spec §6.2). */
    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public DocumentVerificationParams build() {
      return new DocumentVerificationParams(this);
    }
  }
}
