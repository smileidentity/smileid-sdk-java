# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/smileidentity/smileid-sdk-java/compare/v12.0.0...HEAD
[12.0.0]: https://github.com/smileidentity/smileid-sdk-java/releases/tag/v12.0.0
