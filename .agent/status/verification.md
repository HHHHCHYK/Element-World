# Verification Status

## Current State

The primary project verification path is Gradle. The GitHub Actions workflow runs `./gradlew build` on Ubuntu with Java 21. The local Gradle build config compiles Java with release 17.

## Active Work

For this documentation-only framework update, verification should focus on document structure and diff hygiene. Full Gradle build is optional unless implementation files are changed.

## Verification Checklist

Documentation-only changes:

- `git status --short`
- `git diff --check`
- Manual check that `README.md`, `AGENTS.md`, and `.agent/` docs exist.

Code, Gradle, resource, or metadata changes:

- Windows: `.\gradlew.bat check --console=plain`
- Windows: `.\gradlew.bat build --console=plain`
- Unix-like shell: `./gradlew check --console=plain`
- Unix-like shell: `./gradlew build --console=plain`

## Risks

- The project currently has no dedicated test sources; Gradle may report `test NO-SOURCE`.
- Gradle Wrapper may attempt network access if the distribution is not already cached.
- Sandbox network restrictions can block Wrapper downloads; rerun with approved escalation only when build verification is required.

## Next Steps

- Prefer `check` before `build` when validating style-rule changes.
- Prefer `build` for any Java, Fabric metadata, resource, mixin, or Gradle edits.
- Record newly discovered verification gaps in this file instead of burying them in final chat summaries.