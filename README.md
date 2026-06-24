# ElementWorld

ElementWorld is a Fabric Minecraft mod project for Minecraft 1.20.1. The current codebase focuses on an elemental combat system, including element attachment, elemental reactions, custom damage metadata, HUD rendering, commands, registries, and mixins.

## Requirements

- Java toolchain compatible with the project Gradle configuration. The source and target compatibility are Java 17.
- Gradle Wrapper from this repository.
- Fabric Loom, Fabric Loader, Yarn mappings, and Fabric API versions are declared in `gradle.properties`.

Current key versions:

- Minecraft: `1.20.1`
- Yarn mappings: `1.20.1+build.10`
- Fabric Loader: `0.16.10`
- Fabric API: `0.92.3+1.20.1`

CI currently builds on Ubuntu with Java 21 and runs `./gradlew build`.

## Common Commands

On Windows PowerShell:

```powershell
.\gradlew.bat tasks --console=plain
.\gradlew.bat check --console=plain
.\gradlew.bat build --console=plain
```

On Unix-like shells:

```bash
./gradlew tasks --console=plain
./gradlew check --console=plain
./gradlew build --console=plain
```

`check` includes the project-specific `codeStyleCheck` task when present in `build.gradle`.

## Repository Layout

- `src/main/java/com/elementworld/` - common mod code, commands, element model, damage metadata, reactions, modifiers, shields, registries, and mixins.
- `src/client/java/com/elementworld/` - client entrypoint and HUD rendering.
- `src/main/resources/` - Fabric mod metadata, mixin config, language files, models, icon, and data resources.
- `.github/workflows/build.yml` - CI build workflow.
- `docs/` - human-readable reports and long-form project documents.
- `.agent/` - agent collaboration reference, current status, reusable memory, and archive notes.
- `AGENTS.md` - durable instructions future agents should read first.

## Current Status

The project is initialized as a Fabric Loom Gradle project. The current working tree may contain uncommitted code-quality and metadata changes; future agents should inspect `git status --short` before editing and must not revert user changes unless explicitly requested.