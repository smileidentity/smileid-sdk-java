package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response from GET /v3/services/supported_documents (spec §6.14). */
public final class SupportedDocumentsResponse {

  @JsonProperty("valid_documents")
  private List<CountryDocuments> validDocuments;

  public List<CountryDocuments> getValidDocuments() {
    return validDocuments;
  }
}
