# AGENTS.md

This repository holds Smile ID's V3 server-side SDK for Java.

## Source of truth

The API surface (endpoints, request and response shapes) comes from the OpenAPI specifications published at [smileidentity/api-reference](https://github.com/smileidentity/api-reference). Check those specs before adding or changing any client behaviour.

## Layout

- `src/main/java/com/smileidentity/generated/` — wire models and thin per-operation functions. A client generator will own this tree later; keep it free of hand-written business logic and expect it to be regenerated wholesale.
- `src/main/java/com/smileidentity/client/` — hand-written client code: `SmileID` (builder plus config), the transport (headers, auth, retries, multipart serialization) and the resource namespaces.
- `src/main/java/com/smileidentity/errors/` — hand-written error types and the error-body parser.
- `src/main/java/com/smileidentity/helpers/` — hand-written helpers: `BinaryInput`, validators, and the `waitUntilComplete` polling helper.

Public method and parameter names are camelCase; wire field names are always verbatim snake_case, mapped with Jackson `@JsonProperty` at the serialization boundary. Never rename a wire field.

## Running tests

```bash
./gradlew build   # compiles, runs tests, checks formatting (Spotless) and generated/ coverage
./gradlew test    # tests only
```

`./gradlew build` fails unless `generated/` has 100% line and branch coverage (JaCoCo). The HTML report is in `build/reports/jacoco/test/html/`. Add tests to close a gap; do not exclude code from coverage.

The `Quality gates` workflow also compares the public API with the newest stable `vX.Y.Z` tag that is an ancestor of the base branch (`./gradlew japicmp`, job `api-compat`). It fails on a break the base branch doesn't already have, unless the pull request is labelled `breaking`. On bot pull requests it also requires 100% coverage of changed lines (`diff-cover`) and no new coverage exclusions (`scripts/check-coverage-exclusions.sh`). `scripts/bump-version.sh <major|minor|patch>` bumps the version and dates the changelog for a release.

Tests are offline and use OkHttp MockWebServer. The end-to-end sandbox test (`EndToEndTest`) skips unless `SMILE_PARTNER_ID` and `SMILE_API_KEY` are set in the environment.

Compilation needs a working JDK 11: the root and `example/` toolchains are both `JavaLanguageVersion.of(11)`. No toolchain download repository is configured, so Gradle will not fetch one, and the build fails with "Cannot find a Java installation ... languageVersion=11". Run `./gradlew javaToolchains` to see which JDKs Gradle found and why it rejected any. For example, on Apple Silicon an x86_64 JDK 11 without Rosetta shows as invalid. Install an arm64 JDK 11, such as Temurin 11, rather than changing the toolchain line. If you do change it locally, do not commit it, and say in the pull request that you did not compile on Java 11.

`-PtestJavaVersion=<n>` only chooses the JVM the tests run on (the default is 11). It does not remove the need for JDK 11. CI runs `./gradlew build test -PtestJavaVersion=11` and `=21`, then `./gradlew -p example build`.

## Org-wide agent conventions

For agent conventions that apply across Smile ID repositories, see [smileidentity/agents](https://github.com/smileidentity/agents) (private repository, internal contributors only).
