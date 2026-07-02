package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** One document id type inside GET /v3/services/supported_documents (spec §6.14). */
public final class DocumentIdType {

  @JsonProperty("code")
  private String code;

  @JsonProperty("name")
  private String name;

  @JsonProperty("example")
  private List<String> example;

  @JsonProperty("has_back")
  private Boolean hasBack;

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public List<String> getExample() {
    return example;
  }

  public Boolean getHasBack() {
    return hasBack;
  }
}
