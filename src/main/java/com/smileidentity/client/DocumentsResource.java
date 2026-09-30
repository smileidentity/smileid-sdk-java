package com.smileidentity.client;

import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import com.smileidentity.generated.models.ResidencyDocumentVerificationParams;
import com.smileidentity.generated.operations.DocumentOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.documents()} — document verification endpoints (spec §4, §6.2, §6.3). */
public final class DocumentsResource {

  private final Transport transport;

  DocumentsResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/document_verification. */
  public AcceptedResponse verify(DocumentVerificationParams params) {
    return verify(params, RequestOptions.none());
  }

  public AcceptedResponse verify(DocumentVerificationParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    return DocumentOperations.documentVerification(transport, params, options);
  }

  /** POST /v3/enhanced_document_verification — id_type is required (spec §6.3). */
  public AcceptedResponse verifyEnhanced(EnhancedDocumentVerificationParams params) {
    return verifyEnhanced(params, RequestOptions.none());
  }

  public AcceptedResponse verifyEnhanced(
      EnhancedDocumentVerificationParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    Validators.requireIdType(params.getIdType());
    return DocumentOperations.enhancedDocumentVerification(transport, params, options);
  }

  /** POST /v3/residency_document_verification — passport plus the visa endorsed in it. */
  public AcceptedResponse verifyResidency(ResidencyDocumentVerificationParams params) {
    return verifyResidency(params, RequestOptions.none());
  }

  public AcceptedResponse verifyResidency(
      ResidencyDocumentVerificationParams params, RequestOptions options) {
    Validators.requireEmailOrPhone(params.getUserDetails());
    Validators.requireResidencyInputs(params.getIdType(), params.getVisa());
    return DocumentOperations.residencyDocumentVerification(
        transport, withPassportDefault(params), options);
  }

  // Applied here rather than in generated/ so regeneration cannot drop it.
  private static ResidencyDocumentVerificationParams withPassportDefault(
      ResidencyDocumentVerificationParams p) {
    if (p.getIdType() != null && !p.getIdType().isEmpty()) {
      return p;
    }
    return ResidencyDocumentVerificationParams.builder()
        .selfieImage(p.getSelfieImage())
        .livenessImages(p.getLivenessImages())
        .document(p.getDocument())
        .documentBack(p.getDocumentBack())
        .visa(p.getVisa())
        .consent(p.getConsent())
        .country(p.getCountry())
        .idType("PASSPORT")
        .userDetails(p.getUserDetails())
        .callbackUrl(p.getCallbackUrl())
        .partnerParams(p.getPartnerParams())
        .metadata(p.getMetadata())
        .userId(p.getUserId())
        .build();
  }
}
