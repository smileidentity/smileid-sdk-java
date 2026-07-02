package com.smileidentity.generated.models;

/** Comparison image type for POST /v3/compare (spec §6.7). */
public enum ComparisonImageType {
  DOCUMENT,
  ID_PHOTO,
  PORTRAIT;

  /** Verbatim wire value. */
  public String wireValue() {
    return name();
  }
}
