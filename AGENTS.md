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
./gradlew build   # compiles, runs tests and checks formatting (Spotless)
./gradlew test    # tests only
```

Tests are offline and use OkHttp MockWebServer. The end-to-end sandbox test (`EndToEndTest`) skips unless `SMILE_PARTNER_ID` and `SMILE_API_KEY` are set in the environment.

## Org-wide agent conventions

For agent conventions that apply across Smile ID repositories, see [smileidentity/agents](https://github.com/smileidentity/agents) (private repository, internal contributors only).
