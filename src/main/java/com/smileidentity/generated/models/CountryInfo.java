package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Country descriptor inside GET /v3/services/supported_documents (spec §6.14). */
public final class CountryInfo {

  @JsonProperty("code")
  private String code;

  @JsonProperty("name")
  private String name;

  @JsonProperty("continent")
  private String continent;

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getContinent() {
    return continent;
  }
}
