package com.smileidentity.client;

import com.smileidentity.generated.models.BankCodesResponse;
import com.smileidentity.generated.models.IdStatusResponse;
import com.smileidentity.generated.models.SupportedDocumentsParams;
import com.smileidentity.generated.models.SupportedDocumentsResponse;
import com.smileidentity.generated.models.SupportedIdTypesResponse;
import com.smileidentity.generated.operations.ServicesOperations;

/** {@code client.services()} — the /v3/services endpoints (spec §4, §6.12–§6.15). */
public final class ServicesResource {

  private final Transport transport;

  ServicesResource(Transport transport) {
    this.transport = transport;
  }

  /** GET /v3/services/bank_codes (no auth). */
  public BankCodesResponse bankCodes() {
    return bankCodes(null, RequestOptions.none());
  }

  public BankCodesResponse bankCodes(String country) {
    return bankCodes(country, RequestOptions.none());
  }

  public BankCodesResponse bankCodes(String country, RequestOptions options) {
    return ServicesOperations.bankCodes(transport, country, options);
  }

  /** GET /v3/services/supported_id_types (no auth). */
  public SupportedIdTypesResponse supportedIdTypes() {
    return supportedIdTypes(null, RequestOptions.none());
  }

  public SupportedIdTypesResponse supportedIdTypes(String country) {
    return supportedIdTypes(country, RequestOptions.none());
  }

  public SupportedIdTypesResponse supportedIdTypes(String country, RequestOptions options) {
    return ServicesOperations.supportedIdTypes(transport, country, options);
  }

  /** GET /v3/services/supported_documents (no auth). */
  public SupportedDocumentsResponse supportedDocuments() {
    return supportedDocuments(null, RequestOptions.none());
  }

  public SupportedDocumentsResponse supportedDocuments(SupportedDocumentsParams params) {
    return supportedDocuments(params, RequestOptions.none());
  }

  public SupportedDocumentsResponse supportedDocuments(
      SupportedDocumentsParams params, RequestOptions options) {
    return ServicesOperations.supportedDocuments(transport, params, options);
  }

  /** GET /v3/services/id_status (token required). */
  public IdStatusResponse idStatus(String country, String idType) {
    return idStatus(country, idType, RequestOptions.none());
  }

  public IdStatusResponse idStatus(String country, String idType, RequestOptions options) {
    return ServicesOperations.idStatus(transport, country, idType, options);
  }
}
