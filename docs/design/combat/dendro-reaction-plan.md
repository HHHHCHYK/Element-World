# 草反应实现策划

## 定位

本文件是给实现 AI / 工程实现者使用的策划说明，不是代码实现方案。

本轮目标是补齐 Dendro / 草元素反应，使其反应关系按原神官方逻辑成立。重点是反应系统完整性，不优先扩展草元素的世界生态来源。

## 设计目标

- 草元素进入正式反应体系。
- 玩家能理解并验证：燃烧、绽放、烈绽放、超绽放、原激化、超激化、蔓激化。
- 草反应不要求一次性完成植物生态、草方块附着、作物系统或新怪物。
- 反应表现要能被玩家观察到，至少需要浮动文字、伤害数字或等价反馈。

## 反应关系

### Burning / 燃烧

- 触发：Dendro + Pyro，或 Pyro + Dendro。
- 行为：目标进入持续燃烧状态，周期性受到 Pyro 伤害。
- 设计要求：
  - 可以沿用当前 Combustion / 燃烧持续伤害逻辑。
  - 需要避免反应伤害再次触发附着和新反应。
  - 燃烧应在草附着耗尽后结束。

### Bloom / 绽放

- 触发：Dendro + Hydro，或 Hydro + Dendro。
- 行为：在目标附近生成 Dendro Core / 草原核。
- 设计要求：
  - 草原核不是普通附着元素，而是独立的短生命周期反应产物。
  - 草原核到期后爆炸，造成范围 Dendro 伤害。
  - 同一世界同时存在的草原核应有上限；超出上限时最旧草原核立即爆炸。
  - 首个实现不要求注册可见实体，可先用运行时对象、浮动文字和伤害反馈表达。

### Burgeon / 烈绽放

- 触发：Pyro 接触或命中草原核。
- 行为：草原核立即爆炸，造成范围 Dendro 伤害。
- 设计要求：
  - 范围和伤害应高于普通 Bloom，体现 Pyro 引爆。
  - 伤害类型仍应视为 Dendro 反应伤害。
  - 触发后移除该草原核。

### Hyperbloom / 超绽放

- 触发：Electro 接触或命中草原核。
- 行为：草原核转化为追踪式单体 Dendro 伤害。
- 设计要求：
  - 首个实现可不做真实飞行物，允许直接选择草原核附近最近目标结算伤害。
  - 优先避免命中触发者本人；如果没有其他目标，再允许命中可用目标。
  - 触发后移除该草原核。

### Quicken / 原激化

- 触发：Dendro + Electro，或 Electro + Dendro。
- 行为：目标获得 Quicken / 原激化状态。
- 设计要求：
  - 当前代码中的 Catalyze 元素可作为 Quicken 状态使用。
  - Quicken 是后续超激化和蔓激化的前置状态，不应只作为一次性伤害。
  - Quicken 状态应随时间衰减。

### Aggravate / 超激化

- 触发：Electro 命中带有 Quicken 的目标。
- 行为：本次 Electro 攻击获得额外反应伤害。
- 设计要求：
  - 这是追加伤害，不是蒸发/融化那种倍率增幅。
  - 应保留目标身上的 Quicken 状态，除非现有元素量规则要求自然消耗。
  - 需要有明确反馈，例如显示 Aggravate / 超激化。

### Spread / 蔓激化

- 触发：Dendro 命中带有 Quicken 的目标。
- 行为：本次 Dendro 攻击获得额外反应伤害。
- 设计要求：
  - 这是追加伤害，不是蒸发/融化倍率增幅。
  - Spread 的追加收益应高于 Aggravate。
  - 需要有明确反馈，例如显示 Spread / 蔓激化。

## 不反应关系

按官方逻辑，以下组合不应产生草反应：

- Dendro + Cryo：不反应。
- Dendro + Anemo：不扩散草。
- Dendro + Geo：不结晶草。

如果当前实现中存在 Anemo 扩散 Dendro 或 Geo 结晶 Dendro，应在草反应补全时移除或禁用。

## 数值方向

本策划只规定相对关系，不规定最终平衡数值。

- Burning：持续低频或中频伤害，重点是持续压力。
- Bloom：延迟范围伤害，作为基础绽放收益。
- Burgeon：范围爆发，应强于 Bloom。
- Hyperbloom：单体高命中收益，应强于 Bloom，但范围弱于 Burgeon。
- Aggravate：Electro 追加伤害。
- Spread：Dendro 追加伤害，收益高于 Aggravate。

实现者应使用 ElementWorld 现有反应伤害尺度适配，不直接照搬原神等级公式。

## 反馈要求

最低反馈要求：

- 触发反应时显示反应名称。
- 反应伤害有对应元素颜色的伤害数字。
- 草原核生成、爆炸、转化至少有文字或粒子反馈之一。

优先显示名称：

- Burning
- Bloom
- Burgeon
- Hyperbloom
- Quicken
- Aggravate
- Spread

## 验收标准

- Dendro + Pyro 双方向均触发 Burning。
- Dendro + Hydro 双方向均生成草原核。
- 草原核到期触发 Bloom。
- Pyro 触发草原核时产生 Burgeon。
- Electro 触发草原核时产生 Hyperbloom。
- Dendro + Electro 双方向均生成 Quicken。
- Electro 命中 Quicken 触发 Aggravate。
- Dendro 命中 Quicken 触发 Spread。
- Cryo、Anemo、Geo 与 Dendro 不触发草反应。
- 反应造成的二次伤害不会递归触发新的附着和反应。
- 构建通过，并完成游戏内手动验证。

## 非本轮目标

- 草元素世界生态来源。
- 植物、树叶、草方块、作物相关附着。
- 新 Dendro 生物、新群系、新方块。
- 草原核实体模型、碰撞、动画和持久化。
- 完整生存获取和平衡。
