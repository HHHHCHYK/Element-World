# Agent Instructions for ElementWorld

## Communication

- Default to Chinese for user-facing explanations. Keep commands, paths, class names, Gradle task names, and API names in their original spelling.
- Be concise in final summaries, but include verification results and any skipped checks.
- Before changing files, explain the intended edit briefly.

## Environment Notes

- The project root is a Windows workspace. Prefer PowerShell commands when using the shell.
- Terminal output may render UTF-8 Chinese as mojibake. Treat this as a display issue unless file bytes or build output prove otherwise.
- Prefer `rg` / `rg --files` for searching.
- Use `apply_patch` for manual edits when it works. If the sandbox helper hangs or fails, use a controlled workspace-bounded PowerShell write and mention that in the result.

## Project Structure

- Common mod code lives under `src/main/java/com/elementworld/`.
- Client code lives under `src/client/java/com/elementworld/`.
- Fabric metadata, mixin configs, lang files, models, data, and icon live under `src/main/resources/` and `src/client/resources/`.
- Current collaboration state belongs in `.agent/status/`; durable rules belong in `.agent/reference/`; topic-specific reusable findings belong in `.agent/memory/`; historical notes belong in `.agent/archive/`.

## Engineering Rules

- Inspect the current worktree before editing. The repository may already contain user or agent changes.
- Never run `git reset --hard`, `git checkout --`, or other destructive rollback commands unless the user explicitly asks.
- Do not revert unrelated user changes. Work with nearby changes if they affect the task.
- Follow existing Fabric, Gradle, Java 17, and Minecraft 1.20.1 conventions.
- Keep Java names idiomatic: classes and enums in PascalCase, enum constants in UPPER_SNAKE_CASE, methods and fields in lowerCamelCase.
- Avoid `System.out`; use `ElementWorld.LOGGER` for runtime diagnostics.
- Avoid wildcard imports and hard-coded user-facing strings when a lang key is appropriate.

## Verification

- For documentation-only edits, run `git diff --check` and inspect `git status --short`.
- For Java, Gradle, resource, or build config edits, run `./gradlew build` or `.\gradlew.bat build --console=plain` depending on shell.
- If touching quality rules, run `check` as well as `build`.
- Note that the project currently has no dedicated test source set; Gradle may report `test NO-SOURCE`.

## Delivery

- Summaries should identify created/merged docs, validation run, and any remaining risks.
- Link to `.agent/status/current-status.md` for the latest state rather than repeating long status histories here.