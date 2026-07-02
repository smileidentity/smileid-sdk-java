package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** One country entry inside GET /v3/services/supported_documents (spec §6.14). */
public final class CountryDocuments {

  @JsonProperty("country")
  private CountryInfo country;

  @JsonProperty("id_types")
  private List<DocumentIdType> idTypes;

  public CountryInfo getCountry() {
    return country;
  }

  public List<DocumentIdType> getIdTypes() {
    return idTypes;
  }
}
