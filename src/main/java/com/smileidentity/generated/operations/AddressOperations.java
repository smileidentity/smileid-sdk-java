package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AddressAcceptedResponse;
import com.smileidentity.generated.models.AddressVerificationParams;
import java.util.ArrayList;
import java.util.List;

/** Thin operation function for POST /v3/address. Not retryable. */
public final class AddressOperations {

  private AddressOperations() {}

  public static AddressAcceptedResponse addressVerification(
      Transport transport, AddressVerificationParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "address", params.getAddress());
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_number", params.getIdNumber());
    OperationSupport.addText(parts, "utility_number", params.getUtilityNumber());
    OperationSupport.addText(parts, "utility_provider", params.getUtilityProvider());
    OperationSupport.addText(parts, "utility_type", params.getUtilityType());
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
            .path("/v3/address")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, AddressAcceptedResponse.class);
  }
}
