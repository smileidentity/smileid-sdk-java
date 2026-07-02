package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.BankCodesResponse;
import com.smileidentity.generated.models.IdStatusResponse;
import com.smileidentity.generated.models.SupportedDocumentsParams;
import com.smileidentity.generated.models.SupportedDocumentsResponse;
import com.smileidentity.generated.models.SupportedIdTypesResponse;

/**
 * Thin per-operation functions for the /v3/services endpoints (spec §6.12–§6.15). The three list
 * endpoints are unauthenticated; id_status requires a token. All are idempotent GETs and therefore
 * retryable.
 */
public final class ServicesOperations {

  private ServicesOperations() {}

  public static BankCodesResponse bankCodes(
      Transport transport, String country, RequestOptions options) {
    ApiRequest.Builder b =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/services/bank_codes")
            .authenticated(false)
            .idempotent(true)
            .options(options);
    if (country != null) {
      b.query("country", country);
    }
    return transport.execute(b.build(), BankCodesResponse.class);
  }

  public static SupportedIdTypesResponse supportedIdTypes(
      Transport transport, String country, RequestOptions options) {
    ApiRequest.Builder b =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/services/supported_id_types")
            .authenticated(false)
            .idempotent(true)
            .options(options);
    if (country != null) {
      b.query("country", country);
    }
    return transport.execute(b.build(), SupportedIdTypesResponse.class);
  }

  public static SupportedDocumentsResponse supportedDocuments(
      Transport transport, SupportedDocumentsParams params, RequestOptions options) {
    ApiRequest.Builder b =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/services/supported_documents")
            .authenticated(false)
            .idempotent(true)
            .options(options);
    if (params != null) {
      if (params.getContinent() != null) {
        b.query("continent", params.getContinent());
      }
      if (params.getCountryCode() != null) {
        b.query("country_code", params.getCountryCode());
      }
      if (params.getLocale() != null) {
        b.query("locale", params.getLocale());
      }
    }
    return transport.execute(b.build(), SupportedDocumentsResponse.class);
  }

  public static IdStatusResponse idStatus(
      Transport transport, String country, String idType, RequestOptions options) {
    ApiRequest request =
        ApiRequest.builder()
            .method("GET")
            .path("/v3/services/id_status")
            .authenticated(true)
            .idempotent(true)
            .query("country", country)
            .query("id_type", idType)
            .options(options)
            .build();
    return transport.execute(request, IdStatusResponse.class);
  }
}
