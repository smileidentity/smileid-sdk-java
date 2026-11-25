package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.SmileLinkCreateParams;
import com.smileidentity.generated.models.SmileLinkCreated;
import com.smileidentity.generated.models.SmileLinkListParams;
import com.smileidentity.generated.models.SmileLinkListResponse;
import com.smileidentity.generated.models.SmileLinkResource;
import com.smileidentity.generated.models.SmileLinkUpdateParams;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin operation functions for the /v3/smile_links endpoints. The token has no product claim. The
 * two GETs are retryable; create and update are not.
 */
public final class SmileLinkOperations {

  private SmileLinkOperations() {}

  public static SmileLinkCreated createSmileLink(
      Transport transport, SmileLinkCreateParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addJson(parts, "id_types", params.getIdTypes());
    OperationSupport.addText(parts, "name", params.getName());
    OperationSupport.addText(parts, "company_name", params.getCompanyName());
    OperationSupport.addText(
        parts, "notice_privacy_policy_url", params.getNoticePrivacyPolicyUrl());
    OperationSupport.addBoolean(parts, "is_single_use", params.getIsSingleUse());
    OperationSupport.addText(
        parts,
        "callback_url",
        OperationSupport.effectiveCallbackUrl(transport, params.getCallbackUrl(), options));
    OperationSupport.addText(parts, "description", params.getDescription());
    OperationSupport.addText(parts, "expires_at", params.getExpiresAt());
    OperationSupport.addText(parts, "logo_url", params.getLogoUrl());
    OperationSupport.addText(parts, "redirect_url", params.getRedirectUrl());
    OperationSupport.addText(parts, "theme_color", params.getThemeColor());
    OperationSupport.addJson(parts, "partner_params", params.getPartnerParams());
    OperationSupport.addText(parts, "user_id", params.getUserId());
    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/smile_links")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, SmileLinkCreated.class);
  }

  public static SmileLinkResource getSmileLink(
      Transport transport, String refId, RequestOptions options) {
    ApiRequest request =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/smile_links/" + OperationSupport.encodePathSegment(refId))
            .authenticated(true)
            .idempotent(true)
            .options(options)
            .build();
    return transport.execute(request, SmileLinkResource.class);
  }

  public static SmileLinkResource updateSmileLink(
      Transport transport, String refId, SmileLinkUpdateParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addJson(parts, "id_types", params.getIdTypes());
    OperationSupport.addBoolean(parts, "is_disabled", params.getIsDisabled());
    ApiRequest request =
        ApiRequest.builder()
            .method("PUT")
            .path("/v3/smile_links/" + OperationSupport.encodePathSegment(refId))
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, SmileLinkResource.class);
  }

  public static SmileLinkListResponse listSmileLinks(
      Transport transport, SmileLinkListParams params, RequestOptions options) {
    ApiRequest.Builder b =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/smile_links")
            .authenticated(true)
            .idempotent(true)
            .options(options);
    if (params != null) {
      if (params.getLimit() != null) {
        b.query("limit", String.valueOf(params.getLimit()));
      }
      if (params.getPage() != null) {
        b.query("page", String.valueOf(params.getPage()));
      }
      if (params.getSingleUse() != null) {
        b.query("single_use", String.valueOf(params.getSingleUse()));
      }
    }
    return transport.execute(b.build(), SmileLinkListResponse.class);
  }
}
