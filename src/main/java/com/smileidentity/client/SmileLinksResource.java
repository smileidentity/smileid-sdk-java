package com.smileidentity.client;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.SmileLinkCreateParams;
import com.smileidentity.generated.models.SmileLinkCreated;
import com.smileidentity.generated.models.SmileLinkListParams;
import com.smileidentity.generated.models.SmileLinkListResponse;
import com.smileidentity.generated.models.SmileLinkResource;
import com.smileidentity.generated.models.SmileLinkUpdateParams;
import com.smileidentity.generated.operations.SmileLinkOperations;
import java.util.List;

/**
 * {@code client.smileLinks()} — the /v3/smile_links endpoints. Their tokens carry no product claim.
 */
public final class SmileLinksResource {

  private final Transport transport;

  SmileLinksResource(Transport transport) {
    this.transport = transport;
  }

  /**
   * POST /v3/smile_links — creates a link and returns its id and URL. Never retried. A callback URL
   * is sent when the request options, the params or the client's default callback URL have one.
   */
  public SmileLinkCreated create(SmileLinkCreateParams params) {
    return create(params, RequestOptions.none());
  }

  public SmileLinkCreated create(SmileLinkCreateParams params, RequestOptions options) {
    requireIdTypes(params.getIdTypes());
    requireText(params.getName(), "name");
    requireText(params.getNoticePrivacyPolicyUrl(), "notice_privacy_policy_url");
    if (params.getIsSingleUse() == null) {
      throw new ValidationException("is_single_use is required");
    }
    return SmileLinkOperations.createSmileLink(transport, params, options);
  }

  /** GET /v3/smile_links/{refId} — reads one link. */
  public SmileLinkResource get(String refId) {
    return get(refId, RequestOptions.none());
  }

  public SmileLinkResource get(String refId, RequestOptions options) {
    requireText(refId, "ref_id");
    return SmileLinkOperations.getSmileLink(transport, refId, options);
  }

  /**
   * PUT /v3/smile_links/{refId} — changes a link's id types and/or disables or re-enables it, and
   * returns the updated link. Never retried.
   */
  public SmileLinkResource update(String refId, SmileLinkUpdateParams params) {
    return update(refId, params, RequestOptions.none());
  }

  public SmileLinkResource update(
      String refId, SmileLinkUpdateParams params, RequestOptions options) {
    requireText(refId, "ref_id");
    if (params.getIdTypes() == null && params.getIsDisabled() == null) {
      throw new ValidationException("at least one of id_types or is_disabled is required");
    }
    if (params.getIdTypes() != null) {
      requireIdTypes(params.getIdTypes());
    }
    return SmileLinkOperations.updateSmileLink(transport, refId, params, options);
  }

  /** GET /v3/smile_links — lists the partner's links, newest first. */
  public SmileLinkListResponse list() {
    return list(null, RequestOptions.none());
  }

  public SmileLinkListResponse list(SmileLinkListParams params) {
    return list(params, RequestOptions.none());
  }

  public SmileLinkListResponse list(SmileLinkListParams params, RequestOptions options) {
    return SmileLinkOperations.listSmileLinks(transport, params, options);
  }

  private static void requireIdTypes(List<?> idTypes) {
    if (idTypes == null || idTypes.isEmpty()) {
      throw new ValidationException("id_types must contain at least one entry");
    }
  }

  private static void requireText(String value, String name) {
    if (value == null || value.trim().isEmpty()) {
      throw new ValidationException(name + " is required");
    }
  }
}
