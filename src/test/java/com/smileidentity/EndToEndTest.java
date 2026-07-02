package com.smileidentity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.smileidentity.client.Environment;
import com.smileidentity.client.SmileID;
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.JobStatus;
import com.smileidentity.generated.models.UserDetails;
import com.smileidentity.helpers.WaitOptions;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Sandbox end-to-end test (spec §11 gate item 5): submits an Enhanced KYC job and polls it to
 * completion. Reads SMILE_PARTNER_ID and SMILE_API_KEY from the environment and skips cleanly (does
 * not fail) when they are unset. Credential values are never printed or logged.
 */
class EndToEndTest {

  @Test
  void sandboxEnhancedKycCompletesEndToEnd() {
    String partnerId = System.getenv("SMILE_PARTNER_ID");
    String apiKey = System.getenv("SMILE_API_KEY");
    assumeTrue(
        partnerId != null && !partnerId.isEmpty() && apiKey != null && !apiKey.isEmpty(),
        "SMILE_PARTNER_ID and SMILE_API_KEY are not set; skipping the sandbox E2E test");

    SmileID smile =
        SmileID.builder()
            .partnerId(partnerId)
            .apiKey(apiKey)
            .environment(Environment.SANDBOX)
            .build();

    // The sandbox only accepts recognized test identities, matched on
    // given_names + last_name + email.
    AcceptedResponse accepted =
        smile
            .enhancedKyc()
            .verify(
                EnhancedKycParams.builder()
                    .country("NG")
                    .idType("NIN")
                    .idNumber("12345678901")
                    .userDetails(
                        UserDetails.builder()
                            .givenNames("Amina Fatou")
                            .lastName("Clearwater")
                            .email("amina.clearwater@example.com")
                            .build())
                    .consent(Consent.granted(Instant.now(), "EN", "https://example.com/privacy"))
                    .build());

    assertTrue(accepted.isAccepted(), "sandbox must accept the Enhanced KYC submission");
    assertNotNull(accepted.getJobId());

    JobStatus status =
        smile
            .verifications()
            .waitUntilComplete(
                accepted.getJobId(),
                WaitOptions.builder()
                    .interval(Duration.ofSeconds(2))
                    .timeout(Duration.ofMinutes(2))
                    .build());
    assertTrue(
        status.isComplete(), "job should reach a terminal state, got: " + status.getStatus());
  }
}
