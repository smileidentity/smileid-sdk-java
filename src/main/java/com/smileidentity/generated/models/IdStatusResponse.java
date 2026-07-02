package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from GET /v3/services/id_status (spec §6.15). */
public final class IdStatusResponse {

  @JsonProperty("last_checked")
  private String lastChecked;

  @JsonProperty("last_check_status")
  private String lastCheckStatus;

  @JsonProperty("last_hour_success_rate")
  private String lastHourSuccessRate;

  @JsonProperty("last_known_status")
  private String lastKnownStatus;

  @JsonProperty("last_check_success_rate")
  private String lastCheckSuccessRate;

  public String getLastChecked() {
    return lastChecked;
  }

  public String getLastCheckStatus() {
    return lastCheckStatus;
  }

  public String getLastHourSuccessRate() {
    return lastHourSuccessRate;
  }

  public String getLastKnownStatus() {
    return lastKnownStatus;
  }

  public String getLastCheckSuccessRate() {
    return lastCheckSuccessRate;
  }
}
