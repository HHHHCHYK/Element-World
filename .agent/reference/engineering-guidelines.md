# Engineering Guidelines

## Purpose

Define durable engineering rules for future ElementWorld changes.

## Current Rule

Use the repository's existing Fabric Loom, Java 17, and Minecraft 1.20.1 patterns. Keep changes scoped to the subsystem being edited and preserve user work already present in the working tree.

Core guidelines:

- Prefer explicit imports over wildcard imports.
- Use idiomatic Java names: PascalCase classes, UPPER_SNAKE_CASE enum constants, lowerCamelCase methods and fields.
- Keep user-facing text in language files when it is visible to players or command users.
- Use `ElementWorld.LOGGER` instead of `System.out` or raw stack traces.
- Keep server/client boundaries clear: common code in `src/main`, client-only rendering and client networking in `src/client`.
- Register network receivers once during initialization, not inside per-frame callbacks.
- Do not add generated build outputs, Loom run data, IDE folders, logs, or local environment files to Git.
- Keep `.agent/status/` current when a task changes near-term risks or verification expectations.

## Rationale

The mod mixes gameplay logic, Fabric metadata, client rendering, mixins, and resources. Small naming or registration mistakes can compile but fail at runtime. These rules protect maintainability and reduce repeated cleanup work.

## Application

Apply this reference when touching Java source, Fabric metadata, mixin configs, resources, Gradle tasks, `.gitignore`, or agent workflow docs.

## Verification

- Run `git diff --check` for all edits.
- Run `.\gradlew.bat check --console=plain` after style-rule or verification changes.
- Run `.\gradlew.bat build --console=plain` after Java, Gradle, resource, mixin, or metadata changes.
- Search for banned patterns when relevant: `System.out`, `printStackTrace(`, wildcard imports, stale Fabric example text, and Java file names containing underscores.