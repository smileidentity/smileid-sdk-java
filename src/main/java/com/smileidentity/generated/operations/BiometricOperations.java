package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.EnrollParams;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin operation functions for POST /v3/biometric_kyc, /v3/registration, /v3/authentication and
 * /v3/compare (spec §6.4–§6.7). None are retryable.
 */
public final class BiometricOperations {

  private BiometricOperations() {}

  public static AcceptedResponse biometricKyc(
      Transport transport, BiometricKycParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_type", params.getIdType());
    OperationSupport.addText(parts, "id_number", params.getIdNumber());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addNumber(parts, "sandbox_result", params.getSandboxResult());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/biometric_kyc")
            .authenticated(true)
            .partnerIdHeader(true)
            .userIdHeader(params.getUserId())
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }

  public static AcceptedResponse registration(
      Transport transport, EnrollParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addBoolean(parts, "allow_new_enroll", params.getAllowNewEnroll());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addNumber(parts, "sandbox_result", params.getSandboxResult());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/registration")
            .authenticated(true)
            .userIdHeader(params.getUserId())
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }

  public static AcceptedResponse authentication(
      Transport transport, AuthenticationParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    // Routing note (§6.6): user_id goes in the body, not the User-ID header.
    OperationSupport.addText(parts, "user_id", params.getUserId());
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addBoolean(parts, "use_enrolled_image", params.getUseEnrolledImage());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addNumber(parts, "sandbox_result", params.getSandboxResult());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/authentication")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }

  public static AcceptedResponse compare(
      Transport transport, CompareParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBinary(
        parts, "selfie_image", params.getSelfieImage(), "selfie.jpg", OperationSupport.IMAGE_JPEG);
    OperationSupport.addBinary(
        parts,
        "comparison_image",
        params.getComparisonImage(),
        "comparison.jpg",
        OperationSupport.IMAGE_JPEG);
    if (params.getComparisonImageType() != null) {
      OperationSupport.addText(
          parts, "comparison_image_type", params.getComparisonImageType().wireValue());
    }
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addLivenessImages(parts, params.getLivenessImages());
    OperationSupport.addBoolean(parts, "allow_new_enroll", params.getAllowNewEnroll());
    // Routing note (§6.7): user_id is an optional body field.
    OperationSupport.addText(parts, "user_id", params.getUserId());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addNumber(parts, "sandbox_result", params.getSandboxResult());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/compare")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }
}
