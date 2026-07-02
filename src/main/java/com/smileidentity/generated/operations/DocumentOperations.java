package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin operation functions for POST /v3/document_verification and POST
 * /v3/enhanced_document_verification (spec §6.2, §6.3). Both require the SmileID-Partner-ID header.
 * Not retryable.
 */
public final class DocumentOperations {

  private DocumentOperations() {}

  public static AcceptedResponse documentVerification(
      Transport transport, DocumentVerificationParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addBinary(
        parts, "document", params.getDocument(), "document.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addBinary(
        parts,
        "document_back",
        params.getDocumentBack(),
        "document_back.jpg",
        OperationSupport.IMAGE_JPEG);
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_type", params.getIdType());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/document_verification")
            .authenticated(true)
            .partnerIdHeader(true)
            .userIdHeader(params.getUserId())
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }

  public static AcceptedResponse enhancedDocumentVerification(
      Transport transport, EnhancedDocumentVerificationParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addBinary(
        parts, "document", params.getDocument(), "document.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addBinary(
        parts,
        "document_back",
        params.getDocumentBack(),
        "document_back.jpg",
        OperationSupport.IMAGE_JPEG);
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_type", params.getIdType());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/enhanced_document_verification")
            .authenticated(true)
            .partnerIdHeader(true)
            .userIdHeader(params.getUserId())
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }
}
