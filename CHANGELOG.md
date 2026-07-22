# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Security

- `baseUrl` must now be an absolute https URL with no query or fragment; the
  builder rejects anything else and there is no insecure override.
- Callback URLs (`defaultCallbackUrl` and per-request `callbackUrl`) must be
  https, validated at construction and before send.
- `job_id` and `user_id` path parameters are percent-encoded as single path
  segments, so a hostile id cannot alter the request path.
- Caller-supplied multipart content types are validated before send; invalid
  values (for example containing CR/LF) raise `ValidationException`. Hostile
  filenames are covered by tests (OkHttp escapes CR, LF and quotes).

### Changed

- A 2xx response whose body is not a JSON object now raises the new
  `UnexpectedResponseException` instead of a generic parse failure.
- The offline test suite runs against a TLS MockWebServer (okhttp-tls,
  test-only dependency).
- Renamed the Maven artifact from `smile-identity-core` to `usesmileid-java`
  (`-java` avoids colliding with the Android SDK's coordinates); the
  coordinates are now `com.smileidentity:usesmileid-java`. The
  `com.smileidentity` package namespace is unchanged.
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
