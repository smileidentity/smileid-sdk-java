package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * One verification configuration of a Smile Link: a country, an ID type and the verification method
 * to run for it. Sent as an element of the {@code id_types} part and returned on link resources.
 * {@code verificationMethod} is one of biometric_kyc, document_verification,
 * enhanced_document_verification or enhanced_kyc; the API returns Document Verification as {@code
 * doc_verification}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"country", "id_type", "verification_method"})
public final class SmileLinkIdType {

  @JsonProperty("country")
  private String country;

  @JsonProperty("id_type")
  private String idType;

  @JsonProperty("verification_method")
  private String verificationMethod;

  // For Jackson, when a link resource is parsed.
  private SmileLinkIdType() {}

  public SmileLinkIdType(String country, String idType, String verificationMethod) {
    this.country = country;
    this.idType = idType;
    this.verificationMethod = verificationMethod;
  }

  public String getCountry() {
    return country;
  }

  public String getIdType() {
    return idType;
  }

  public String getVerificationMethod() {
    return verificationMethod;
  }
}
