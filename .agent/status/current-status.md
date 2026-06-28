# Current Status

> 最近更新：2026-06-25 — 反应系统重构（阶段 A→F 完成 + 编译通过）

## Current State

ElementWorld 是一个 Fabric mod（Minecraft 1.20.1 / Java 17）。仓库早期已建立
`README.md` / `AGENTS.md` / `.agent/` 协作框架。

最近完成的重要工作是**元素反应系统重构**（详见 `reaction-refactor-plan.md`）：
反应逻辑与伤害逻辑已彻底解耦，反应在被附着者容器侧查表结算，
伤害侧 Mixin 只读 `ReactionOutcome` 改伤害倍率；新增与 Damage 解耦的无伤害施加入口。

工作树当前状态：`git status` 显示未提交的修改集中在
`src/main/java/com/elementworld/`（重构涉及的 Java 源码）与 `.agent/status/`（本批文档），
另有预先存在、与本任务无关的未提交改动（如 `LivingEntityMixin.java` 的行尾符变化、
`.agents/` 目录）——按 AGENTS.md 规定不回滚这些无关改动。

## Active Work

反应系统重构已完成阶段 A→F 并通过 `gradlew build`（含 `codeStyleCheck`）：

- 新建：`ReactionOutcome` / `ReactionContext` / `ReactionHandler` / `ReactionTable`
  / `ElementApplicationService`
- 修改：`ElementContainer`（`resolveReaction` 解耦入口 + handler 辅助方法）、
  `LivingEntityMixin`（按 outcome 分发）、`Combustion`（die 同步修复）、
  `Commands`（`/element add` 删 `damage(0.1f)` hack 改调无伤害入口）

详见 `todolist-2026-06-25.md`（含完成标记）与 `reaction-refactor-plan.md`（实施记录）。

## Verification Checklist

- [x] `.\gradlew.bat build --console=plain` → BUILD SUCCESSFUL（含 `codeStyleCheck` / `check`）
- [x] `test NO-SOURCE`（项目无单测，符合预期）
- [x] `git diff --check` 无空白错误
- [ ] **手动 DEBUG 回归未执行**（需进游戏，回归清单见 `todolist-2026-06-25.md` 末尾）
  - 蒸发 4 方向 / 感电两路径 / 燃烧两路径 / 冻结两方向 / 激化两方向
  - 超导 / 扩散 / 结晶 / 绽放
  - [BUG] 回归：触发感电/燃烧的那次攻击 amount 走增伤×减抗
  - `/element add` 不再造成 0.1 伤害

## Risks

- 工作树有预先存在的、与反应重构无关的未提交改动；未来 agent 不得在未经确认时回滚它们。
- PowerShell/Git Bash 可能将 UTF-8 中文渲染为 mojibake，属显示问题，除非字节/构建输出证明否则不改文件。
- 持续反应（感电/燃烧）状态不持久化：存档重载后丢失（已知设计，本次未动）。
- 空壳反应（Crystallize / Bloom / Catalyze）与未实现反应（SHATTER / BURGEON / HYPERBLOOM）保持现状。

## Next Steps

- 执行手动 DEBUG 回归（需游戏内操作，agent 无法代跑）。
- 若回归通过，可推进发布文档 `docs/release-discussions/2026-06-25-discussion-01-release-plan.md`
  的后续阶段：给 `ElementApplicationService` 接上元素附魔 + `AttackEntityCallback` 触发源。
- 本状态文件随进度变化更新；新增稳定结论沉淀到 `.agent/memory/`。
