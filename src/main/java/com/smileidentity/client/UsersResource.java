package com.smileidentity.client;

import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.ReportUserFraudResponse;
import com.smileidentity.generated.operations.UserOperations;
import com.smileidentity.helpers.Validators;

/** {@code client.users()} — fraud reporting (spec §4, §6.11). */
public final class UsersResource {

  private final Transport transport;

  UsersResource(Transport transport) {
    this.transport = transport;
  }

  /** POST /v3/users/{user_id}/report_fraud. Multipart, never auto-retried. */
  public ReportUserFraudResponse reportFraud(String userId, ReportFraudParams params) {
    return reportFraud(userId, params, RequestOptions.none());
  }

  public ReportUserFraudResponse reportFraud(
      String userId, ReportFraudParams params, RequestOptions options) {
    Validators.validateReportFraud(params);
    return UserOperations.reportFraud(transport, userId, params, options);
  }

  /** Convenience wrapper: report fraud with is_fraud=true (spec §4). */
  public ReportUserFraudResponse flagFraud(
      String userId, FraudReason reason, String notes, String reportedBy) {
    return reportFraud(
        userId,
        ReportFraudParams.builder()
            .isFraud(true)
            .reason(reason)
            .notes(notes)
            .reportedBy(reportedBy)
            .build());
  }

  /** Convenience wrapper: clear a fraud flag with is_fraud=false (spec §4). */
  public ReportUserFraudResponse clearFraud(String userId, String notes, String reportedBy) {
    return reportFraud(
        userId,
        ReportFraudParams.builder().isFraud(false).notes(notes).reportedBy(reportedBy).build());
  }
}
