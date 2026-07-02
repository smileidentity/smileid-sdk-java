package com.smileidentity.generated.models;

/** Fraud reason enum for POST /v3/users/{user_id}/report_fraud (spec §6.11). */
public enum FraudReason {
  FIRST_PARTY_FRAUD,
  SECOND_PARTY_FRAUD,
  THIRD_PARTY_FRAUD,
  SYNTHETIC_IDENTITY,
  ACCOUNT_TAKEOVER,
  DOCUMENT_FORGERY,
  IDENTITY_FARMING,
  MULE_ACCOUNT,
  OTHER;

  /** Verbatim wire value. */
  public String wireValue() {
    return name();
  }
}
