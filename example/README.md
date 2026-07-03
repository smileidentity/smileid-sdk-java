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
export SMILE_PARTNER_ID="12345"
export SMILE_API_KEY="..."
export SMILE_CALLBACK_URL="https://your-app.example.com/smile-callback"
```

Optional:

- `SMILE_BASE_URL` overrides the SDK environment URL.
- `SMILE_TIMEOUT_MS` sets the per-request timeout.

## Commands

```bash
../gradlew run --args="services --country NG"
../gradlew run --args="enhanced-kyc --country NG --id-type NIN --id-number 12345678901 --given-names Amina --last-name Okafor --email amina@example.com --privacy-url https://your-app.example.com/privacy"
../gradlew run --args="status --job-id job_..."
../gradlew run --args="replay --job-id job_... --callback-url https://your-app.example.com/smile-callback"
```

## Development

```bash
../gradlew test
../gradlew spotlessCheck
```
