# Code Quality Audit Memory

## Topic

2026-06-24 ElementWorld code-quality audit and cleanup.

## Durable Context

A prior audit found and addressed recurring issues around Fabric example leftovers, debug output, Java naming, client HUD receiver registration, missing HUD assets, container state handling, and damage modification wiring. The long-form report is stored at `docs/code-quality-audit-2026-06-24.md`.

Key reusable conclusions:

- Avoid reintroducing Fabric template text such as `Hello Fabric world`, example descriptions, or example mixins.
- Avoid `System.out`; use the project logger.
- Keep Java source names free of underscores.
- Keep command/user-facing text translatable when practical.
- Network receivers should be registered during initialization, not in render or tick loops.
- Damage changes in `ModifyArgs` must be written back to the `Args` object.

## Practical Rule

Before finishing work that touches source code or metadata, run the project style/verification checks available in Gradle and search for the recurring banned patterns when relevant.

## Related Files

- `docs/code-quality-audit-2026-06-24.md`
- `build.gradle`
- `src/client/java/com/elementworld/gui/ElementRender.java`
- `src/main/java/com/elementworld/mixin/livingEntity/LivingEntityMixin.java`
- `src/main/resources/fabric.mod.json`