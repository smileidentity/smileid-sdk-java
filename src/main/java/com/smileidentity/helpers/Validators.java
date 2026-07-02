package com.smileidentity.helpers;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.UserDetails;

/** Client-side validation raised before any request is sent (spec §5.1, §6.6, §6.11). */
public final class Validators {

  private Validators() {}

  /** At least one of email / phone_number must be present on user_details (spec §5.1). */
  public static void requireEmailOrPhone(UserDetails userDetails) {
    if (userDetails == null) {
      throw new ValidationException("user_details is required");
    }
    boolean hasEmail = userDetails.getEmail() != null && !userDetails.getEmail().isEmpty();
    boolean hasPhone =
        userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().isEmpty();
    if (!hasEmail && !hasPhone) {
      throw new ValidationException("user_details requires at least one of email or phone_number");
    }
  }

  /** Images are required on authentication unless use_enrolled_image is true (spec §6.6). */
  public static void validateAuthenticationImages(AuthenticationParams params) {
    if (params.getUserId() == null || params.getUserId().isEmpty()) {
      throw new ValidationException("user_id is required for authentication");
    }
    boolean useEnrolled = Boolean.TRUE.equals(params.getUseEnrolledImage());
    if (!useEnrolled) {
      if (params.getSelfieImage() == null) {
        throw new ValidationException("selfie_image is required unless use_enrolled_image is true");
      }
      if (params.getLivenessImages() == null || params.getLivenessImages().isEmpty()) {
        throw new ValidationException(
            "liveness_images are required unless use_enrolled_image is true");
      }
    }
  }

  /**
   * Conditional report_fraud rules (spec §6.11): reason required when is_fraud=true; notes required
   * when is_fraud=false or reason=OTHER.
   */
  public static void validateReportFraud(ReportFraudParams params) {
    if (params.getIsFraud() == null) {
      throw new ValidationException("is_fraud is required");
    }
    if (params.getReportedBy() == null || params.getReportedBy().isEmpty()) {
      throw new ValidationException("reported_by is required");
    }
    boolean hasNotes = params.getNotes() != null && !params.getNotes().isEmpty();
    if (params.getIsFraud()) {
      if (params.getReason() == null) {
        throw new ValidationException("reason is required when is_fraud is true");
      }
      if (params.getReason() == FraudReason.OTHER && !hasNotes) {
        throw new ValidationException("notes are required when reason is OTHER");
      }
    } else {
      if (!hasNotes) {
        throw new ValidationException("notes are required when is_fraud is false");
      }
    }
    if (params.getNotes() != null && params.getNotes().length() > 500) {
      throw new ValidationException("notes must be 500 characters or fewer");
    }
  }
}
