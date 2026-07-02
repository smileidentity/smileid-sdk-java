package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from POST /v3/token (spec §6.0). Internal: never exposed to partners. */
public final class TokenResponse {

  @JsonProperty("token")
  private String token;

  public String getToken() {
    return token;
  }
}
