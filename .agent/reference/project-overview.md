# Project Overview

## Purpose

Describe the stable project identity, technology stack, important entry points, and verified build baseline for ElementWorld.

## Current Rule

ElementWorld is a Fabric Minecraft mod targeting Minecraft 1.20.1. The Gradle project uses Fabric Loom, Java 17 source/target compatibility, and the Gradle Wrapper checked into the repository.

Stable entry points and resources:

- Main mod initializer: `com.elementworld.ElementWorld`
- Client initializer: `com.elementworld.ElementWorldClient`
- Fabric metadata: `src/main/resources/fabric.mod.json`
- Common mixins: `src/main/resources/elementworld.mixins.json`
- Client mixins: `src/client/resources/elementworld.client.mixins.json`
- Main Java code: `src/main/java/com/elementworld/`
- Client Java code: `src/client/java/com/elementworld/`
- Assets and data: `src/main/resources/assets/elementworld/` and `src/main/resources/data/elementworld/`

## Rationale

Future agents need a stable map of the repository so they do not infer the wrong stack, edit the wrong source set, or change Fabric metadata without updating matching entrypoints and resources.

## Application

Use this reference before changing initialization, registries, mixin wiring, resources, commands, HUD behavior, damage metadata, or build configuration.

## Verification

- `fabric.mod.json` entrypoints match the Java classes that exist in source.
- Mixin config class names match source files under `src/main/java/com/elementworld/mixin/` and `src/client/java/` when applicable.
- `.\gradlew.bat build --console=plain` or `./gradlew build --console=plain` succeeds after code or resource changes.
- `.github/workflows/build.yml` remains aligned with the Gradle build command.