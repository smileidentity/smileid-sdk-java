package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One bank code entry from GET /v3/services/bank_codes (spec §6.12). */
public final class BankCode {

  @JsonProperty("code")
  private String code;

  @JsonProperty("country")
  private String country;

  @JsonProperty("name")
  private String name;

  public String getCode() {
    return code;
  }

  public String getCountry() {
    return country;
  }

  public String getName() {
    return name;
  }
}
