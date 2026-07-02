# Smile ID Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.smileidentity/smile-identity-core?label=Maven%20Central)](https://central.sonatype.com/artifact/com.smileidentity/smile-identity-core)
[![CI](https://img.shields.io/github/actions/workflow/status/smileidentity/smileid-sdk-java/test.yml?branch=main&label=CI)](https://github.com/smileidentity/smileid-sdk-java/actions/workflows/test.yml)
[![License](https://img.shields.io/github/license/smileidentity/smileid-sdk-java)](LICENSE)

Official Smile ID server-side SDK for Java — V3 APIs.

This repository is under active development. The SDK is not yet published, and the API surface may change without notice.

- Group: `com.smileidentity`
- Artifact: `smile-identity-core`
- Requires Java 11 or later

## Install

Not yet published to Maven Central. Once published:

Gradle:

```kotlin
implementation("com.smileidentity:smile-identity-core:0.1.0")
```

Maven:

```xml
<dependency>
  <groupId>com.smileidentity</groupId>
  <artifactId>smile-identity-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

Until then, build from source with `./gradlew build` and use the jar from `build/libs/`.

## Create a client

Construct one `SmileID` client with your partner id and API key. The SDK manages authentication for you: it fetches an internal token, caches it and refreshes it when it expires. You never handle tokens yourself.

```java
import com.smileidentity.client.Environment;
import com.smileidentity.client.SmileID;

SmileID smile = SmileID.builder()
    .partnerId("1234")
    .apiKey(System.getenv("SMILE_API_KEY"))
    .environment(Environment.SANDBOX)
    .defaultCallbackUrl("https://app.example.com/callback")
    .build();
```

Configuration options:

| Option | Default | Notes |
|---|---|---|
| `partnerId` | required | Numeric string, no leading zeros |
| `apiKey` | required | Partner API key |
| `environment` | `SANDBOX` | `SANDBOX` or `PRODUCTION` |
| `partnerSecret` | unset | Enables HMAC request signing when set (see below) |
| `defaultCallbackUrl` | unset | Used when a call omits `callbackUrl` |
| `baseUrl` | derived | Explicit override; wins over `environment` |
| `timeout` | 30 seconds | Per-request total timeout |
| `maxRetries` | 2 | Idempotent operations only |
| `httpClient` | SDK default | Inject your own `OkHttpClient` |

## Environments

The client targets the sandbox by default. Select production explicitly:

- `Environment.SANDBOX` → `https://testapi.smileidentity.com`
- `Environment.PRODUCTION` → `https://api.smileidentity.com`

## Shared models

Most verification calls need end-user details and consent:

```java
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.UserDetails;
import java.time.Instant;

UserDetails user = UserDetails.builder()
    .givenNames("John")
    .lastName("Doe")
    .email("john@example.com")   // at least one of email or phoneNumber is required
    .build();

Consent consent = Consent.granted(Instant.now(), "EN", "https://example.com/privacy");
```

Binary inputs (selfies, liveness frames, documents) accept a `File`, a `byte[]` or an `InputStream` via `BinaryInput.of(...)`, with optional `.withFilename(...)` and `.withContentType(...)`.

## Operations

### Enhanced KYC

```java
import com.smileidentity.generated.models.AcceptedResponse;
import com.smileidentity.generated.models.EnhancedKycParams;

AcceptedResponse accepted = smile.enhancedKyc().verify(EnhancedKycParams.builder()
    .country("NG").idType("NIN").idNumber("12345678901")
    .userDetails(user)
    .consent(consent)
    .userId("user_01h8x9y2z3a4b5c6d7e8f9g0h1")
    .build());
accepted.getJobId();     // "job_..."
accepted.isAccepted();   // true — normalizes "Accepted" and "accepted"
```

`smile.kyc().enhanced(params)` is an equivalent alias.

### Document verification

```java
import com.smileidentity.generated.models.DocumentVerificationParams;
import com.smileidentity.helpers.BinaryInput;
import java.io.File;
import java.util.Arrays;
import java.util.List;

List<BinaryInput> livenessFrames = Arrays.asList(
    BinaryInput.of(new File("live1.jpg")),
    BinaryInput.of(new File("live2.jpg")),
    BinaryInput.of(new File("live3.jpg")),
    BinaryInput.of(new File("live4.jpg")),
    BinaryInput.of(new File("live5.jpg")),
    BinaryInput.of(new File("live6.jpg")));   // 6 to 8 frames

AcceptedResponse accepted = smile.documents().verify(DocumentVerificationParams.builder()
    .selfieImage(BinaryInput.of(new File("selfie.jpg")))
    .livenessImages(livenessFrames)
    .document(BinaryInput.of(new File("doc.jpg")))
    .country("NG")                             // idType optional: auto-classified
    .userDetails(user)
    .consent(consent)
    .build());
```

### Enhanced document verification

Same shape as document verification, but `idType` is required:

```java
import com.smileidentity.generated.models.EnhancedDocumentVerificationParams;

AcceptedResponse accepted = smile.documents().verifyEnhanced(
    EnhancedDocumentVerificationParams.builder()
        .selfieImage(BinaryInput.of(new File("selfie.jpg")))
        .livenessImages(livenessFrames)
        .document(BinaryInput.of(new File("doc.jpg")))
        .country("NG").idType("PASSPORT")
        .userDetails(user)
        .consent(consent)
        .build());
```

### Biometric KYC

```java
import com.smileidentity.generated.models.BiometricKycParams;

AcceptedResponse accepted = smile.biometricKyc().verify(BiometricKycParams.builder()
    .selfieImage(BinaryInput.of(new File("selfie.jpg")))
    .livenessImages(livenessFrames)
    .country("NG").idType("NIN").idNumber("12345678901")
    .userDetails(user)
    .consent(consent)
    .build());
```

### Biometric enrollment

```java
import com.smileidentity.generated.models.EnrollParams;

AcceptedResponse accepted = smile.biometric().enroll(EnrollParams.builder()
    .selfieImage(BinaryInput.of(new File("selfie.jpg")))
    .livenessImages(livenessFrames)
    .userDetails(user)
    .consent(consent)
    .userId("user-42")
    .build());
```

### Biometric authentication

`userId` is required and must match an enrolled user. Images are required unless `useEnrolledImage` is true.

```java
import com.smileidentity.generated.models.AuthenticationParams;

AcceptedResponse accepted = smile.biometric().authenticate(AuthenticationParams.builder()
    .userId("user-42")
    .selfieImage(BinaryInput.of(new File("selfie.jpg")))
    .livenessImages(livenessFrames)
    .userDetails(user)
    .consent(consent)
    .build());
```

### Selfie compare

```java
import com.smileidentity.generated.models.CompareParams;
import com.smileidentity.generated.models.ComparisonImageType;

AcceptedResponse accepted = smile.biometric().compare(CompareParams.builder()
    .selfieImage(BinaryInput.of(new File("selfie.jpg")))
    .comparisonImage(BinaryInput.of(new File("id_photo.jpg")))
    .comparisonImageType(ComparisonImageType.ID_PHOTO)
    .userDetails(user)
    .consent(consent)
    .build());
```

### Job status

```java
import com.smileidentity.generated.models.JobStatus;

JobStatus status = smile.verifications().retrieve("job_01h2xcejqtf2nbrexx3vqjhp41");
status.isComplete();    // terminal
status.isProcessing();  // still running
status.isNotFound();    // a 404 returns this status instead of raising
```

### Wait for completion

```java
import com.smileidentity.helpers.WaitOptions;
import java.time.Duration;

JobStatus done = smile.verifications().waitUntilComplete(
    accepted.getJobId(),
    WaitOptions.builder()
        .interval(Duration.ofSeconds(2))
        .timeout(Duration.ofSeconds(60))
        .treatNotFoundAsPending(true)
        .build());
```

Raises `com.smileidentity.errors.TimeoutException` if the deadline passes.

### Replay a callback

```java
import com.smileidentity.generated.models.ReplayCallbackResponse;
import com.smileidentity.generated.models.ReplayParams;

ReplayCallbackResponse replayed = smile.verifications().replay(
    "job_01h2xcejqtf2nbrexx3vqjhp41",
    ReplayParams.builder().callbackUrl("https://app.example.com/callback").build());
```

A 409 means the verification is still processing; it raises `ConflictException` and is never retried automatically.

### Report user fraud

```java
import com.smileidentity.generated.models.FraudReason;
import com.smileidentity.generated.models.ReportFraudParams;

smile.users().reportFraud("user-42", ReportFraudParams.builder()
    .isFraud(true)
    .reason(FraudReason.ACCOUNT_TAKEOVER)
    .notes("Suspicious takeover pattern")
    .reportedBy("fraud-team@example.com")
    .build());

// Convenience wrappers:
smile.users().flagFraud("user-42", FraudReason.DOCUMENT_FORGERY, null, "fraud-team@example.com");
smile.users().clearFraud("user-42", "False positive after review", "fraud-team@example.com");
```

### Services

```java
import com.smileidentity.generated.models.SupportedDocumentsParams;

// No authentication needed for these three:
smile.services().bankCodes("NG");
smile.services().supportedIdTypes("NG");
smile.services().supportedDocuments(SupportedDocumentsParams.builder()
    .continent("AFRICA").countryCode("NG").locale("en-GB").build());

// Token required:
smile.services().idStatus("NG", "NIN");
```

## Error handling

All errors extend `com.smileidentity.errors.SmileIDException` and expose `getStatusCode()`, `getStatus()`, `getMessage()`, `getCode()`, `getRequestId()` and `getRawBody()`.

| Class | When |
|---|---|
| `InvalidRequestException` | HTTP 400, 415 |
| `ValidationException` | Client-side validation, raised before sending |
| `AuthenticationException` | HTTP 401 (after one automatic token refresh) |
| `PaymentRequiredException` | HTTP 402: insufficient wallet balance |
| `PermissionException` | HTTP 403 |
| `NotFoundException` | HTTP 404 (never from `verifications().retrieve`) |
| `ConflictException` | HTTP 409: never auto-retried |
| `PayloadTooLargeException` | HTTP 413 |
| `RateLimitException` | HTTP 429 |
| `ApiException` | HTTP 5xx |
| `ConnectionException` | Network failure or timeout, no HTTP response |
| `TimeoutException` | `waitUntilComplete` deadline passed |

```java
import com.smileidentity.errors.PaymentRequiredException;
import com.smileidentity.errors.SmileIDException;

try {
  smile.enhancedKyc().verify(params);
} catch (PaymentRequiredException e) {
  // top up the wallet
} catch (SmileIDException e) {
  System.err.println(e.getStatusCode() + ": " + e.getMessage());
}
```

Retries: idempotent operations (status and services reads, plus the internal token fetch) are retried automatically on connection errors and HTTP 408, 429 and 5xx, with exponential backoff and support for `Retry-After`. Job-creating POSTs are never retried automatically — a connection failure surfaces as `ConnectionException` and the caller decides.

## Telemetry

Every request carries `SmileID-Source-SDK: java`, `SmileID-Source-SDK-Version` and a `User-Agent` string identifying the SDK and Java runtime versions. These headers are observability metadata only; they are never used for authentication.

## Request signing (HMAC)

Off by default. Setting `partnerSecret` on the builder enables signing: each request gains `SmileID-Timestamp` and `SmileID-Request-Signature` headers computed over the exact serialized request body. The exact signature construction is provisional and must be confirmed with Smile ID before relying on it in production.

## Contributing

See [AGENTS.md](AGENTS.md) for how the codebase is laid out and how to run the test suite.

## Security

See [SECURITY.md](SECURITY.md) for how to report a vulnerability.

## License

Licensed under the [MIT License](LICENSE).
