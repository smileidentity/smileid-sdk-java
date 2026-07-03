# AGENTS.md

This repository is a standalone example application for the Smile ID Java SDK.

## Development rules

- Use only the public `com.smileidentity` SDK API.
- Keep tests deterministic with injected OkHttp clients; do not require real Smile ID credentials.
- Keep credentials out of source control and docs.
- Run `../gradlew test` before handing off changes.

## Layout

- `src/main/java/com/smileidentity/example/ExampleApp.java` contains command parsing and SDK calls.
- `src/main/java/com/smileidentity/example/Main.java` is the CLI entrypoint.
- `src/test/java/com/smileidentity/example/ExampleAppTest.java` is the SDK testbench.
- `.github/workflows/ci.yml` runs Gradle tests, Spotless, and Semgrep.
