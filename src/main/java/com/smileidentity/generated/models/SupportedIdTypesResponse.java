package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response from GET /v3/services/supported_id_types (spec §6.13). */
public final class SupportedIdTypesResponse {

  @JsonProperty("id_types")
  private List<SupportedIdType> idTypes;

  public List<SupportedIdType> getIdTypes() {
    return idTypes;
  }
}
