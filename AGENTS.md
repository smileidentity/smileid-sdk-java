# AGENTS.md

This repository holds Smile ID's V3 server-side SDK for Java.

## Source of truth

The API surface (endpoints, request and response shapes) comes from the OpenAPI specifications published at [smileidentity/api-reference](https://github.com/smileidentity/api-reference). Check those specs before adding or changing any client behaviour.

## Layout

- `src/main/java/com/smileidentity/generated/` — will hold generator-owned code once the client generator lands. Do not hand-edit files here once that exists.
- `src/main/java/com/smileidentity/client/` — hand-written client code that wraps the generated layer.
- `src/main/java/com/smileidentity/errors/` — hand-written error types.
- `src/main/java/com/smileidentity/helpers/` — hand-written helper utilities.

At this stage the repository is a scaffold, so most of these directories don't exist yet. They'll be created as the corresponding code is added.

## Running tests

```bash
./gradlew test
```

## Org-wide agent conventions

For agent conventions that apply across Smile ID repositories, see [smileidentity/agents](https://github.com/smileidentity/agents) (private repository, internal contributors only).
