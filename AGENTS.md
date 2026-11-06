# AGENTS.md

This repository holds Smile ID's V3 server-side SDK for Java.

## Source of truth

The API surface (endpoints, request and response shapes) comes from `specs/v3/openapi.yaml` in [smileidentity/api-reference](https://github.com/smileidentity/api-reference). Read it at a release tag such as `v3.1115.0`, not at `main`. When a task or pull request names a tag, use that one. When the SDK and the spec disagree, the spec wins.

- Ignore anything marked `x-smile-sdk-exclude: true`. These are server-to-server details, such as the HMAC signature headers, that the SDKs leave out on purpose.
- Don't add endpoints or fields that aren't in the spec. Internal endpoints are kept out of it on purpose.

## Layout

- `src/main/java/com/smileidentity/generated/` — wire models and thin per-operation functions. This tree is hand-written for now, because there is no generator yet. Edit it by hand, but keep it shaped like generator output: wire models and one thin function per operation, with no business logic. A generator will replace it later.
- `src/main/java/com/smileidentity/client/` — hand-written client code: `SmileID` (builder plus config), the transport (headers, auth, retries, multipart serialization) and the resource namespaces.
- `src/main/java/com/smileidentity/errors/` — hand-written error types and the error-body parser.
- `src/main/java/com/smileidentity/helpers/` — hand-written helpers: `BinaryInput`, validators, and the `waitUntilComplete` polling helper.

Public method and parameter names are camelCase; wire field names are always verbatim snake_case, mapped with Jackson `@JsonProperty` at the serialization boundary. Never rename a wire field.

## Keeping the SDKs in step

This SDK is one of the `smileid-sdk-*` repositories, which share one design. A change to the public API here should be made the same way in the others: the same operations, option names and defaults, adjusted only for each language's naming conventions.

Automated pull requests use these labels and branches:

| Label | Branch | Meaning |
|---|---|---|
| `spec-sync` | `bot/spec-sync` | Brings the SDK up to a new spec tag. One rolling pull request at a time. |
| `breaking` | `bot/spec-sync-breaking-<tag>` | A change that needs a major release. Breaking changes always get their own pull request. |
| `parity-port` | `bot/parity-port-<repo>-<number>` | A change ported from another SDK's merged pull request, which it links to. |
| `release` | `bot/release-v<version>` | A version bump and changelog entry. |

Add `parity:skip` to a pull request that should not be ported to the other SDKs.

Tests:

- Every new operation needs a golden fixture test, which checks the exact request body and headers, and tests that parse its responses.
- Changed lines must be fully covered by tests. Don't add coverage exclusion comments.

## Running tests

```bash
./gradlew build   # compiles, runs tests and checks formatting (Spotless)
./gradlew test    # tests only
```

Tests are offline and use OkHttp MockWebServer. The end-to-end sandbox test (`EndToEndTest`) skips unless `SMILE_PARTNER_ID` and `SMILE_API_KEY` are set in the environment.

Compilation needs a working JDK 11: the root and `example/` toolchains are both `JavaLanguageVersion.of(11)`. No toolchain download repository is configured, so Gradle will not fetch one, and the build fails with "Cannot find a Java installation ... languageVersion=11". Run `./gradlew javaToolchains` to see which JDKs Gradle found and why it rejected any. For example, on Apple Silicon an x86_64 JDK 11 without Rosetta shows as invalid. Install an arm64 JDK 11, such as Temurin 11, rather than changing the toolchain line. If you do change it locally, do not commit it, and say in the pull request that you did not compile on Java 11.

`-PtestJavaVersion=<n>` only chooses the JVM the tests run on (the default is 11). It does not remove the need for JDK 11. CI runs `./gradlew build test -PtestJavaVersion=11` and `=21`, then `./gradlew -p example build`.

## Org-wide agent conventions

For agent conventions that apply across Smile ID repositories, see [smileidentity/agents](https://github.com/smileidentity/agents) (private repository, internal contributors only).
