# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

* `aml()` resource for the AML operations in spec `v3.1165.0`:
  * `oneTime(params)` — `POST /v3/aml`. Returns `OneTimeAmlAcceptedResponse`; the result goes to the callback URL.
  * `oneTimeSync(params)` and `oneTimeSync(params, idempotencyKey, options)` — `POST /v3/aml/sync`. Returns the terminal `OneTimeAmlCallback`. With an idempotency key (sent as `SmileID-Idempotency-Key`) the call is retried like other idempotent calls.
  * `monitoring(params)` — `POST /v3/aml/monitoring`. Returns `AcceptedResponse`.
  * `monitoringNews(jobId, reference, params)` — `GET /v3/aml/monitoring/{job_id}/news/{reference}`. Returns `AmlMonitoringNewsResponse`.
* Tokens for `/v3/aml` and `/v3/aml/sync` are bound to the `one_time_aml` product.

## [13.0.0] - 2026-11-09

This is a major version because it removes `useEnrolledImage` from `AuthenticationParams`. Version 12.2.0 was never published; its changes are in this release.

### Breaking changes

* `AuthenticationParams.useEnrolledImage` is removed. Send the selfie and liveness images to `biometric().authenticate()`, or use `biometricKyc().verifyWithEnrolledImage(params)` to verify against the enrolled selfie.
* A `userId` that is empty or has leading or trailing whitespace now throws `ValidationException` before sending. Trim the value before you pass it.
* A 401 response is no longer retried with a new token. Catch `AuthenticationException` and check your partner ID and API key.

### Changed

* Each request mints its own token, bound to that request's `userId` and product, instead of reusing one cached token.
* The `Transport` throws `UnexpectedResponseException` when the token response has no token, and the token request carries the caller's `RequestOptions` timeout.

### Added

* `BiometricKycResource.verifyWithEnrolledImage(params)` with `userId` verifies against the user's enrolled selfie; the grant is bound into the job's token and the selfie and liveness images may be omitted.

## [12.1.0] - 2026-10-01

### Added

* `documents().verifyResidency()` for Residency Document Verification (`POST /v3/residency_document_verification`). `idType` is optional and defaults to `PASSPORT`.

## [12.0.0] - 2026-08-20

First public release.

### Added

- Verification products: Enhanced KYC, Biometric KYC, Document Verification,
  Enhanced Document Verification, and SmartSelfie enrollment, authentication
  and compare.
- Job status retrieval, with a `waitUntilComplete` helper that polls until a
  job reaches a decision.
- Callback replay for a job.
- Fraud reporting, and the bank codes, supported ID types and supported
  documents lookups.
- Sandbox and production environments, with a `baseUrl` override for any
  other Smile ID host.
- A typed error hierarchy under `com.smileidentity.errors`, one class per
  HTTP status plus connection and timeout failures.

[Unreleased]: https://github.com/smileidentity/smileid-sdk-java/compare/v13.0.0...HEAD
[13.0.0]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.1.0...v13.0.0
[12.1.0]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.0.0...v12.1.0
[12.0.0]: https://github.com/smileidentity/smileid-sdk-java/releases/tag/v12.0.0
