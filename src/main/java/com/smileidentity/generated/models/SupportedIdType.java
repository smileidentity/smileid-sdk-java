package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** One id type entry from GET /v3/services/supported_id_types (spec §6.13). */
public final class SupportedIdType {

  @JsonProperty("bank_code")
  private String bankCode;

  @JsonProperty("country")
  private String country;

  @JsonProperty("label")
  private String label;

  @JsonProperty("regex")
  private String regex;

  @JsonProperty("required_fields")
  private List<String> requiredFields;

  @JsonProperty("type")
  private String type;

  public String getBankCode() {
    return bankCode;
  }

  public String getCountry() {
    return country;
  }

  public String getLabel() {
    return label;
  }

  public String getRegex() {
    return regex;
  }

  public List<String> getRequiredFields() {
    return requiredFields;
  }

  public String getType() {
    return type;
  }
}
