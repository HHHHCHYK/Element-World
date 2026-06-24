# ElementWorld 玩法试玩版收口方案

- 时间：2026-06-25 03:11:44 +08:00
- 讨论编号：第 1 次发布收口讨论

## Summary

目标发布一个“玩法试玩版”，不是技术空壳：玩家通过元素附魔让普通武器获得元素攻击，攻击实体时触发元素附着和反应。附着与玩家攻击行为绑定，但不再依赖 `target.damage()` 才能发生，因此创造模式玩家/免伤目标也能被附着。第一版开放六元素：Pyro、Hydro、Electro、Cryo、Anemo、Geo；Dendro/草反应暂不进入发布玩法。

## Key Changes

- 新增独立元素施加接口，例如 `ElementApplicationService.applyAttackElement(...)`：
  - 输入：攻击者、目标、来源手/物品、元素类型、gauge。
  - 输出：本次元素附着结果和可选 `Reaction`。
  - 该接口不调用 `target.damage()` 来完成附着。
- 玩家攻击入口使用 Fabric `AttackEntityCallback`：
  - 主手物品优先，副手其次。
  - 从元素附魔读取元素类型与等级。
  - 找到元素后调用独立附着接口，然后放行 vanilla 攻击。
- 元素附魔设计：
  - 注册六个互斥附魔：Pyro/Hydro/Electro/Cryo/Anemo/Geo infusion。
  - 等级 I/II/III 对应 gauge `1/2/4`。
  - 使用 `elementworld:elemental_attack_sources` ItemTag 限制可附魔物品，默认包含 vanilla swords/axes。
  - 第一版“基础可获得”：创造栏/命令/附魔书可获得，不要求完整生存概率和平衡。
- 伤害与反应：
  - 附着先在攻击事件中发生，即使目标最终没有受到 vanilla damage 也成立。
  - 本次攻击产生的 `AmpReaction` 结果暂存，随后由伤害修改逻辑消费，用于 Vaporize/Melt 完整倍率。
  - 非 Amp 反应在附着接口内只执行一次，避免攻击事件和 damage Mixin 重复触发。
  - 现有 `DamageSource` 元素机制暂保留给反应伤害、DOT、护盾/抗性结算，但玩家攻击附着不再通过它触发。
- 反馈：
  - 保留现有 HUD 色块。
  - 添加轻量命中/反应反馈：消息、简单粒子或音效任选最低成本实现。
  - Anemo/Geo 保持当前玩法：不附着；攻击无元素目标时不生成附着状态，但需要有视觉反馈说明它们是反应型元素。

## Test Plan

- 元素附魔 I/II/III 分别施加 gauge `1/2/4`。
- 主手和副手都有元素时，主手优先。
- 主手无元素、副手有元素时，副手生效。
- 攻击普通生物时：附着发生，vanilla 伤害继续结算。
- 攻击创造模式玩家或免伤目标时：即使无实际伤害，也能附着元素。
- Vaporize/Melt 能影响本次攻击伤害倍率，且不会重复附着/重复反应。
- ElectroCharged/Combustion 等持续反应只创建一次，重进世界不恢复运行态。
- Anemo/Geo 不附着，但命中已有元素目标时触发对应反应反馈。
- `.\gradlew.bat build --console=plain` 通过。

## Assumptions

- 这次发布定位是“玩法试玩版”，不是完整生存平衡版。
- Dendro、草反应、GUI 完整化、完整 datagen 和完整战利品/附魔台概率可放到后续版本。
- 元素附魔彼此互斥；同一件物品第一版只允许一个元素 infusion。
- 现有 damage Mixin 不立即彻底删除，而是先缩小职责：玩家攻击附着迁出，伤害倍率/反应伤害暂时保留。

