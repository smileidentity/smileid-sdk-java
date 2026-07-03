# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Renamed the Maven artifact from `smile-identity-core` to `smileid`; the
  coordinates are now `com.smileidentity:smileid`. The `com.smileidentity`
  package namespace is unchanged.
- Set the version to 12.0.0 to align the server SDKs with the V12 mobile SDKs.

### Added

- Initial implementation of the V3 server-side SDK: all 14 public operations
  (enhanced KYC, document verification, enhanced document verification,
  biometric KYC, enrollment, authentication, compare, job status, callback
  replay, fraud reporting and the four services endpoints).
- Internal JWT authentication with a thread-safe token cache and a single
  automatic refresh on 401.
- Retry policy for idempotent operations only, honouring `Retry-After`.
- Typed error hierarchy under `com.smileidentity.errors`.
- `verifications().waitUntilComplete(...)` polling helper.
- Optional HMAC request signing, off unless a partner secret is configured.
