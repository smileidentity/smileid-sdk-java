package com.smileidentity.generated.operations;

import com.smileidentity.client.ApiRequest;
import com.smileidentity.client.Part;
import com.smileidentity.client.RequestOptions;
import com.smileidentity.client.Transport;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.ReportUserFraudResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin operation function for POST /v3/users/{user_id}/report_fraud (spec §6.11). Multipart, never
 * retried.
 */
public final class UserOperations {

  private UserOperations() {}

  public static ReportUserFraudResponse reportFraud(
      Transport transport, String userId, ReportFraudParams params, RequestOptions options) {
    List<Part> parts = new ArrayList<>();
    OperationSupport.addBoolean(parts, "is_fraud", params.getIsFraud());
    if (params.getReason() != null) {
      OperationSupport.addText(parts, "reason", params.getReason().wireValue());
    }
    OperationSupport.addText(parts, "notes", params.getNotes());
    OperationSupport.addText(parts, "reported_by", params.getReportedBy());

    ApiRequest request =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/users/" + userId + "/report_fraud")
            .authenticated(true)
            .multipart(parts)
            .options(options)
            .build();
    return transport.execute(request, ReportUserFraudResponse.class);
  }
}
