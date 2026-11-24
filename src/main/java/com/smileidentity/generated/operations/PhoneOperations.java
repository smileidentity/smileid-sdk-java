package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.PhoneVerificationParams;
import java.util.ArrayList;
import java.util.List;

/** Thin operation function for POST /v3/phone. Not retryable. */
public final class PhoneOperations {

  private PhoneOperations() {}

  public static AcceptedResponse phoneVerification(
      Transport transport, PhoneVerificationParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "phone_number", params.getPhoneNumber());
    OperationSupport.addText(parts, "operator", params.getOperator());
    OperationSupport.addText(parts, "id_number", params.getIdNumber());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(parts, "user_id", params.getUserId());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());
    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/phone")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AcceptedResponse.class);
  }
}
