package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.BasicKycParams;
import com.smileidentity.generated.models.BasicKycResult;
import com.smileidentity.helpers.Validators;
import java.util.ArrayList;
import java.util.List;

/** Thin operation functions for POST /v3/basic_kyc and POST /v3/basic_kyc/sync. Not retryable. */
public final class BasicKycOperations {

  private BasicKycOperations() {}

  public static AcceptedResponse basicKyc(
      Transport transport, BasicKycParams params, RequestOptions options) {
    return transport.execute(
        request(
                "/v3/basic_kyc",
                params,
                options,
                OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options))
            .build(),
        AcceptedResponse.class);
  }

  /**
   * The sync endpoint ignores callback_url, so only a callback URL set on the params is sent; the
   * client's default callback URL never is.
   */
  public static BasicKycResult basicKycSync(
      Transport transport, BasicKycParams params, RequestOptions options) {
    Validators.requireHttpsCallbackUrl(params.getCallbackUrl(), "callbackUrl");
    return transport.execute(
        request("/v3/basic_kyc/sync", params, options, params.getCallbackUrl())
            .jobIdFromHeader(true)
            .build(),
        BasicKycResult.class);
  }

  private static ApiRequest.Builder request(
      String path, BasicKycParams params, RequestOptions options, String callbackUrl) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addText(parts, "country", params.getCountry());
    OperationSupport.addText(parts, "id_type", params.getIdType());
    OperationSupport.addText(parts, "id_number", params.getIdNumber());
    OperationSupport.addUserDetailsAndConsent(parts, params.getUserDetails(), params.getConsent());
    OperationSupport.addText(parts, "callback_url", callbackUrl);
    OperationSupport.addText(parts, "bank_code", params.getBankCode());
    OperationSupport.addText(parts, "operator", params.getOperator());
    OperationSupport.addPartnerParamsAndMetadata(
        parts, params.getPartnerParams(), params.getMetadata());
    return ApiRequest.builder()
        .method("POST")
        .path(path)
        .authenticated(true)
        .multipart(parts)
        .options(options);
  }
}
