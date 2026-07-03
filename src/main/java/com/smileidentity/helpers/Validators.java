package com.smileidentity.helpers;

import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.AuthenticationParams;
import com.smileidentity.generated.models.BiometricKycParams;
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.EnrollParams;
import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.ReportFraudParams;
import com.smileidentity.generated.models.UserDetails;
import java.util.List;

/** Client-side validation raised before any request is sent (spec §5.1, §6.6, §6.11). */
public final class Validators {

  private Validators() {}

  /** At least one of email / phone_number must be present on user_details (spec §5.1). */
  public static void requireEmailOrPhone(UserDetails userDetails) {
    if (userDetails == null) {
      throw new ValidationException("user_details is required");
    }
    requireString(userDetails.getGivenNames(), "user_details.given_names");
    requireString(userDetails.getLastName(), "user_details.last_name");
    boolean hasEmail = userDetails.getEmail() != null && !userDetails.getEmail().isEmpty();
    boolean hasPhone =
        userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().isEmpty();
    if (!hasEmail && !hasPhone) {
      throw new ValidationException("user_details requires at least one of email or phone_number");
    }
  }

  public static void validateEnhancedKyc(EnhancedKycParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireString(params.getCountry(), "country");
    requireString(params.getIdType(), "id_type");
    requireString(params.getIdNumber(), "id_number");
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
  }

  public static void validateDocumentVerification(DocumentVerificationParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireString(params.getCountry(), "country");
    requireBinary(params.getSelfieImage(), "selfie_image");
    requireLivenessImages(params.getLivenessImages());
    requireBinary(params.getDocument(), "document");
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
  }

  public static void validateEnhancedDocumentVerification(
      EnhancedDocumentVerificationParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireString(params.getCountry(), "country");
    requireBinary(params.getSelfieImage(), "selfie_image");
    requireLivenessImages(params.getLivenessImages());
    requireBinary(params.getDocument(), "document");
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
    requireIdType(params.getIdType());
  }

  public static void validateBiometricKyc(BiometricKycParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireString(params.getCountry(), "country");
    requireString(params.getIdType(), "id_type");
    requireString(params.getIdNumber(), "id_number");
    requireBinary(params.getSelfieImage(), "selfie_image");
    requireLivenessImages(params.getLivenessImages());
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
  }

  public static void validateEnroll(EnrollParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireBinary(params.getSelfieImage(), "selfie_image");
    requireLivenessImages(params.getLivenessImages());
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
  }

  /** id_type is required on enhanced document verification (spec §6.3). */
  public static void requireIdType(String idType) {
    if (idType == null || idType.isEmpty()) {
      throw new ValidationException("id_type is required for enhanced document verification");
    }
  }

  /** Images are required on authentication unless use_enrolled_image is true (spec §6.6). */
  public static void validateAuthenticationImages(AuthenticationParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    if (params.getUserId() == null || params.getUserId().isEmpty()) {
      throw new ValidationException("user_id is required for authentication");
    }
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
    boolean useEnrolled = Boolean.TRUE.equals(params.getUseEnrolledImage());
    if (!useEnrolled) {
      if (params.getSelfieImage() == null) {
        throw new ValidationException("selfie_image is required unless use_enrolled_image is true");
      }
      requireLivenessImages(params.getLivenessImages());
    }
  }

  public static void validateCompare(CompareParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
    requireBinary(params.getSelfieImage(), "selfie_image");
    requireBinary(params.getComparisonImage(), "comparison_image");
    if (params.getComparisonImageType() == null) {
      throw new ValidationException("comparison_image_type is required");
    }
    if (params.getLivenessImages() != null) {
      requireLivenessImages(params.getLivenessImages());
    }
    requireConsent(params.getConsent());
    requireEmailOrPhone(params.getUserDetails());
  }

  /**
   * Conditional report_fraud rules (spec §6.11): reason required when is_fraud=true; notes required
   * when is_fraud=false or reason=OTHER.
   */
  public static void validateReportFraud(ReportFraudParams params) {
    if (params == null) {
      throw new ValidationException("params are required");
    }
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

  private static void requireConsent(Consent consent) {
    if (consent == null) {
      throw new ValidationException("consent is required");
    }
    if (!Boolean.TRUE.equals(consent.getGranted())) {
      throw new ValidationException("consent.granted must be true");
    }
    requireString(consent.getGrantedAt(), "consent.granted_at");
    requireString(consent.getNoticeLanguage(), "consent.notice_language");
    requireString(consent.getNoticePrivacyPolicyUrl(), "consent.notice_privacy_policy_url");
  }

  private static void requireBinary(Object value, String name) {
    if (value == null) {
      throw new ValidationException(name + " is required");
    }
  }

  private static void requireLivenessImages(List<?> images) {
    if (images == null || images.size() < 6 || images.size() > 8) {
      throw new ValidationException("liveness_images must contain 6 to 8 images");
    }
    for (Object image : images) {
      if (image == null) {
        throw new ValidationException("liveness_images cannot contain null images");
      }
    }
  }

  private static void requireString(String value, String name) {
    if (value == null || value.trim().isEmpty()) {
      throw new ValidationException(name + " is required");
    }
  }
}
