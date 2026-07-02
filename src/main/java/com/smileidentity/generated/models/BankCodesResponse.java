package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response from GET /v3/services/bank_codes (spec §6.12). */
public final class BankCodesResponse {

  @JsonProperty("bank_codes")
  private List<BankCode> bankCodes;

  public List<BankCode> getBankCodes() {
    return bankCodes;
  }
}
