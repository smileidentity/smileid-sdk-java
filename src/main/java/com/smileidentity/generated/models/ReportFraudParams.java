package com.smileidentity.generated.models;

/**
 * Request parameters for POST /v3/users/{user_id}/report_fraud (spec §6.11). {@code reason} is
 * required when {@code isFraud} is true; {@code notes} is required when {@code isFraud} is false or
 * {@code reason} is OTHER.
 */
public final class ReportFraudParams {

  private final Boolean isFraud;
  private final FraudReason reason;
  private final String notes;
  private final String reportedBy;

  private ReportFraudParams(Builder b) {
    this.isFraud = b.isFraud;
    this.reason = b.reason;
    this.notes = b.notes;
    this.reportedBy = b.reportedBy;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Boolean getIsFraud() {
    return isFraud;
  }

  public FraudReason getReason() {
    return reason;
  }

  public String getNotes() {
    return notes;
  }

  public String getReportedBy() {
    return reportedBy;
  }

  public static final class Builder {
    private Boolean isFraud;
    private FraudReason reason;
    private String notes;
    private String reportedBy;

    public Builder isFraud(Boolean isFraud) {
      this.isFraud = isFraud;
      return this;
    }

    public Builder reason(FraudReason reason) {
      this.reason = reason;
      return this;
    }

    /** At most 500 characters (spec §6.11). */
    public Builder notes(String notes) {
      this.notes = notes;
      return this;
    }

    /** Email address of the reporter. */
    public Builder reportedBy(String reportedBy) {
      this.reportedBy = reportedBy;
      return this;
    }

    public ReportFraudParams build() {
      return new ReportFraudParams(this);
    }
  }
}
