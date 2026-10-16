package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.EnhancedKycParams;
import java.util.ArrayList;
import java.util.List;

/** Thin operation function for POST /v3/enhanced_kyc (spec §6.1). Not retryable. */
public final class KycOperations {

  private KycOperations() {}

  public static AcceptedResponse enhancedKyc(
      Transport transport, EnhancedKycParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_type", params.getIdType());
    OperationSupport.addText(parts, "id_number", params.getIdNumber());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addText(parts, "bank_code", params.getBankCode());
    OperationSupport.addText(parts, "operator", params.getOperator());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/enhanced_kyc")
            .authenticated(true)
            .userIdHeader(params.getUserId())
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }
}
