package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HTTP 201 response from POST /v3/smile_links: an acknowledgment with the link's id and the URL to
 * give the end user. Read the full resource with {@code smileLinks().get(refId)}.
 */
public final class SmileLinkCreated {

  @JsonProperty("link")
  private String link;

  @JsonProperty("message")
  private String message;

  @JsonProperty("ref_id")
  private String refId;

  @JsonProperty("user_id")
  private String userId;

  /** The URL to hand to the end user. */
  public String getLink() {
    return link;
  }

  public String getMessage() {
    return message;
  }

  /** The link's id, used on every other smile_links call. */
  public String getRefId() {
    return refId;
  }

  /** The user id a single-use link's job runs under; null on a reusable link. */
  public String getUserId() {
    return userId;
  }
}
