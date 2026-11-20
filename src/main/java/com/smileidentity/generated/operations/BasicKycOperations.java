package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.BasicKycParams;
import com.smileidentity.generated.models.BasicKycResult;
import java.util.ArrayList;
import java.util.List;

/** Thin operation functions for POST /v3/basic_kyc and POST /v3/basic_kyc/sync. Not retryable. */
public final class BasicKycOperations {

  private BasicKycOperations() {}

  public static AcceptedResponse basicKyc(
      Transport transport, BasicKycParams params, RequestOptions options) {
    return transport.execute(
        request("/v3/basic_kyc", transport, params, options), AcceptedResponse.class);
  }

  public static BasicKycResult basicKycSync(
      Transport transport, BasicKycParams params, RequestOptions options) {
    return transport.execute(
        request("/v3/basic_kyc/sync", transport, params, options), BasicKycResult.class);
  }

  private static ApiRequest request(
      String path, Transport transport, BasicKycParams params, RequestOptions options) {
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
    return ApiRequest.builder()
        .method("POST")
        .path(path)
        .authenticated(true)
        .userIdHeader(params.getUserId())
        .multipart(parts)
        .options(options)
        .build();
  }
}
