# Smile ID Java SDK Example

This repository is a small CLI application that demonstrates the public Smile ID Java SDK.

It also acts as a testbench: JUnit runs the same CLI code with an injected OkHttp interceptor and verifies the SDK sends the expected requests.

## Requirements

- Java 11 or later.
- Smile ID sandbox credentials for real API calls.

## Setup

The Gradle build uses a composite build against the sibling SDK checkout:

```kotlin
includeBuild("..")
```

## Configuration

```bash
export SMILE_PARTNER_ID="2"
export SMILE_API_KEY="..."
export SMILE_BASE_URL="https://devapi.smileidentity.com"
export SMILE_CALLBACK_URL="https://your-app.example.com/smile-callback"
```

Partner ids are displayed zero-padded in the portal (for example 002) but must be passed without the leading zeros (2).

`SMILE_BASE_URL` points the SDK at a specific host and wins over the named environment. The SDK only names the sandbox and production, so any other host — a development API, for instance — has to be set this way. Leave it unset to use the sandbox.

Optional:

- `SMILE_TIMEOUT_MS` sets the per-request timeout.

## Commands

```bash
../gradlew run --args="services --country NG"
../gradlew run --args="enhanced-kyc --country NG --id-type NIN --id-number 12345678901 --given-names 'Amina Fatou' --last-name Clearwater --email amina.clearwater@example.com --privacy-url https://your-app.example.com/privacy"
../gradlew run --args="status --job-id job_..."
../gradlew run --args="replay --job-id job_... --callback-url https://your-app.example.com/smile-callback"
```

Non-production environments match test identities on given names, last name and email. The identity above is a recognised test identity and resolves to `clear`; an unrecognised one resolves to `block`.

`status` prints the job's `status` field, which is `processing` while the job runs and then the decision itself — `clear`, `block`, `attention` or `error`.

## Development

```bash
../gradlew test
../gradlew spotlessCheck
```
