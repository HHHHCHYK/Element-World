# 反应系统重构 Plan — 2026-06-25

来源：元素反应抽离 + 查表 + 反应在被附着者结算 + 无伤害施加入口
关联：`todolist-2026-06-25.md`、`docs/release-discussions/2026-06-25-discussion-01-release-plan.md`
本文件是可执行实施 Plan，非调研报告（调研结论见对话记录）。

## 架构原则

**反应逻辑与伤害逻辑彻底解耦。** 元素附着行为发生时，先在被附着者容器侧结算 Reaction，
Reaction 内部完成所有反应行为；伤害侧只"读取" Reaction 结果决定是否改写伤害倍率。
无伤害的附着通过另一条不触发 Damage 的调用路径完成。

```
元素附着行为发生
    │
    ▼
先结算 Reaction（容器侧，与 Damage 无关）
  Reaction 内完成：gauge 扣减 / 衍生元素 / 剧变效果 / 持续反应注册
                  并把"是否增幅、增幅倍率、精通引用"写入 ReactionOutcome
    │
    ▼  ReactionOutcome
    │
┌──── 伤害来源 ────┐
有伤害(damage 流程)   无伤害(ElementApplicationService)
    │                  │
Mixin 读 outcome        不进 Damage
按 outcome 改 amount    Reaction 已做完所有事
```

## 范围

本次做：
- 抽"结算 Reaction"接口（与 Damage 解耦）
- 独立 `ReactionTable` 类 + handler 查表（替换 23 处 instanceof）
- helper 返回 `ReactionOutcome`（三态：NONE / AMPLIFIED / REACTION_OCCURRED）
- 剧变效果 + 持续反应注册全挪进 Reaction 结算阶段
- 无伤害施加入口：**仅接口 + `/element add` 命令改调**
- 伤害侧 Mixin 只读 outcome 改 amount（保留 @ModifyArgs 插入点）
- 冻结返回 Reaction（不再返回 null）

本次不做：
- 附魔系统 / `AttackEntityCallback`（发布文档后续阶段）
- 补全空壳反应（Crystallize / Bloom / Catalyze 的 apply）
- 改 `Element.java` 枚举 switch 散落
- 持续反应重载丢失（已知设计，不动）
- 改 `Reaction` 工厂结构

## 关键决策（已与用户确认）

1. 无伤害施加入口本次仅"接口 + 命令改调"，不接附魔/AttackEntityCallback。
2. 剧变效果 + 持续反应注册全挪进 Reaction 结算阶段，Mixin 不再调 reaction.apply()。
3. helper 返回 ReactionOutcome（非 Reaction 实例）。
4. 查表用独立 ReactionTable 类。
5. 保留 @ModifyArgs 插入点（applyDamage 调用前改写 amount）。
6. 保留 isCannotApply 契约、扩散传染链、持续反应不持久化、OVERLOAD 不对称。

## 实施步骤

### 阶段 A：新增结算核心（纯加法，不改现有行为）

**A1. 新建 `ReactionOutcome`**（`elementComponents/reaction/`）
三态值对象：
- `NONE` — 无反应
- `AMPLIFIED(double multiplier)` — 增幅（蒸发/融化），multiplier = `(1+baseMul)`
- `REACTION_OCCURRED` — 有反应效果但不改伤害（剧变/持续/冻结/空壳）
- 冻结返回 `REACTION_OCCURRED`（不再是 null），冻结副作用（生成 Frozen）在 handler 内完成

**A2. 新建 `ReactionContext`**（record 或不可变类）
- `Element trigger`（后手元素）
- `Element aura`（附着元素）
- `LivingEntity owner`（被附着者）
- `@Nullable DamageSource damageSource`
- `@Nullable ElementContainer attackerContainer`
- `ElementContainer ownerContainer`（供 handler 注册持续反应、elements.add、getMastery）

**A3. 新建 `ReactionHandler`**（函数式接口）
```java
@FunctionalInterface
interface ReactionHandler {
    ReactionOutcome handle(ReactionContext ctx);
}
```

**A4. 新建 `ReactionTable`**（独立类）
- `Map<ElementType, Map<ElementType, ReactionHandler>>` 二维表
- 静态初始化块 `register(trigger, aura, handler)` 注册所有组合
- public `ReactionHandler get(ElementType trigger, ElementType aura)`
- 静态工具方法（消除重复）：
  - `ensureReactionsMap(ElementContainer c)` — 替换 6 处懒加载
  - `registerPersistent(ElementContainer c, Class<? extends Reaction> key, Reaction r)` — 感电/燃烧注册
  - `applyCatalyze(ReactionContext ctx)` — 激化 gauge 扣减 + Catalyze 生成
  - `freezeFormula(gauge, beGauge)` = `Math.min(gauge,beGauge)*2`
- 魔法值常量：`ATTACH_DECAY=0.8`、`STRONG_MUL=2`、`WEAK_MUL=0.5`

### 阶段 B：迁移反应组合到 handler（逐组合对照旧行为）

按下方"反应矩阵迁移对照表"逐条迁移。每条 handler 内完成 gauge 操作 + 元素增删 + reaction 创建，
剧变/持续反应在 handler 内立即执行效果（调 reaction.apply() 或 reactions.put）。
gauge 系数、移除与否、反应类型必须与旧实现逐字节一致。

### 阶段 C：ElementContainer.applyElement 瘦身

`applyElement`（现 207-428，221 行）收缩到约 40-50 行：
1. 设置 owner/attacker/latestAttacker、取 attackerContainer（保留 :208-224）
2. 空容器 → 附着（风岩除外，保留 :228-232）
3. 单元素：
   - 取 aura、gauge/beGauge
   - 同类元素刷新（保留 :244-246）
   - 查表 `handler = ReactionTable.get(triggerType, auraType)`
   - 命中 → `return handler.handle(ctx)`（ctx 含 outcome）
   - 未命中 → addElement 或无操作 + warn
4. 多元素 → 补 warn（当前隐式 null）
5. 返回 ReactionOutcome（签名变更）

**注意签名变更**：`applyElement` 返回类型从 `Reaction` 改为 `ReactionOutcome`。
这会影响 LivingEntityMixin 调用方——阶段 D/F 一并改。

### 阶段 D：抽出与 Damage 解耦的结算入口

新增方法（ElementContainer 或新服务类）：
```java
public ReactionOutcome resolveReaction(Element trigger,
                                       @Nullable LivingEntity attacker,
                                       @Nullable DamageSource source)
```
- 内部完成 owner/attacker 绑定 + 查表结算 + handler 执行
- 不关心调用方是否有伤害
- applyElement 可委托给它，或 applyElement 直接成为它的实现

### 阶段 E：无伤害施加入口（仅接口 + 命令改调）

**E1. 新建 `ElementApplicationService`**（`elementComponents/` 或新包）
```java
public static ReactionOutcome applyElement(LivingEntity target, Element trigger,
                                           @Nullable LivingEntity attacker) {
    // 不调用 target.damage()
    ElementContainer c = ((LivingEntityHolder) target).getElementContainer$EW();
    return c.resolveReaction(trigger, attacker, null);
}
```

**E2. `/element add` 命令改调**（`command/Commands.java:80-85`）
- 删除 `livingEntity.damage(damageSource, 0.1f)` 这条骗附着的 hack
- 改调 `ElementApplicationService.applyElement(target, Element.create(type, gauge), null)`
- 注意：命令现通过 damageSource 设置 element，新接口直接传 Element

### 阶段 F：LivingEntityMixin 瘦身（只读 outcome 改 amount）

`applyDamageModifyArgs`（现 92-223，约 130 行）收缩：
1. 拦截、isInvulnerableTo 检查、取 ownContainer（保留 :92-117）
2. 调容器侧 `resolveReaction` 拿 outcome（替换原 applyElement 调用 :126）
3. 按 outcome 分发（替换 :132-171 的二分逻辑）：
   - `AMPLIFIED` → `amount *= outcome.multiplier × 精通 × 增伤 × 减抗`
   - 其他（NONE / REACTION_OCCURRED）→ amount 不变
   - 无元素攻击 → 物理增伤/减抗（保留 :189-205 的 else 分支）
4. 护盾结算 `applyShield`（保留 :214-218）
5. `args.set(1, amount)`（保留 :222）

**消除**：
- `instanceof AmpReaction` 二分（改读 outcome 类型）
- `reaction.apply()` 调用（已挪进结算阶段）
- 四种重复的"有/无 attacker × 有/无反应"乘区分支
- `getReactionBaseMul` / `getMasteryAmp` 的调用点（移入 AMPLIFIED outcome 构造）

### 阶段 G：编译 + 手动回归

1. `./gradlew build`（Windows: `.\gradlew.bat build --console=plain`）
2. 无单测，用 DEBUG 模式（`ElementWorld.DEBUG`，actionbar 打印元素+gauge）手动回归：
   - 蒸发/融化（Hydro×Pyro、Pyro×Hydro、Pyro×Cryo、Cryo×Pyro）— 确认增幅倍率与 gauge 扣减
   - 感电（Hydro×Electro、Electro×Hydro）— 确认两条路径都注册持续反应且只一次
   - 燃烧（Pyro×Dendro、Dendro×Pyro）— 确认注册
   - 冻结（Hydro×Cryo、Cryo×Hydro）— 确认生成 Frozen 且返回 REACTION_OCCURRED
   - 激化（Electro×Dendro、Dendro×Electro）— 确认生成 Catalyze
   - 超导、扩散、结晶、绽放 — 确认反应类型正确
   - `/element add` — 确认不再造成 0.1 伤害即可附着

## 反应矩阵迁移对照表

trigger=后手元素，aura=附着元素。outcome 列：A=AMPLIFIED(改伤害)，R=REACTION_OCCURRED(不动伤害)，N=NONE。
gauge：g=trigger.gauge，bg=aura.gauge。系数 STRONG=2, WEAK=0.5。

| trigger | aura | 反应 | gauge 操作 | 元素增删 | outcome | 旧代码行 |
|---|---|---|---|---|---|---|
| Hydro | Pyro | VAPORIZE | aura.subGauge(g*2) | — | A(强) | 249-252 |
| Hydro | Cryo | FREEZE | aura.subGauge(g); trigger.subGauge(剩余aura) | add Frozen(min(g,bg)*2) | R | 253-259 |
| Hydro | Frozen | 无反应 | — | addElement(trigger) | N | 260-263 |
| Hydro | Electro | ELECTRO_CHARGED | — | addElement(trigger); 注册EC(**需attackerContainer**) | R | 264-278 |
| Hydro | Dendro | BLOOM | aura.subGauge(g) | — | R(空壳) | 279-285 |
| Pyro | Hydro | VAPORIZE | aura.subGauge(g*0.5) | — | A(弱) | 293-296 |
| Pyro | Electro | OVERLOAD | aura.subGauge(g) | —(不移除) | R | 297-300 |
| Pyro | Cryo | MELT | aura.subGauge(g*2) | — | A(强) | 301-304 |
| Pyro | Frozen | MELT | aura.subGauge(g*2) | — | A(强) | 305-308 |
| Pyro | Dendro | COMBUSTION | — | addElement(trigger); 注册燃烧 | R | 309-320 |
| Electro | Pyro | OVERLOAD | removeElement(aura) | 移除aura(**不对称**) | R | 327-330 |
| Electro | Hydro | ELECTRO_CHARGED | — | addElement(trigger); 注册EC(**用!isElectroCharged判断**) | R | 331-342 |
| Electro | Cryo | SUPERCONDUCT | — | addElement(trigger) | R | 343-346 |
| Electro | Frozen | SUPERCONDUCT | — | addElement(trigger) | R | 343-346 |
| Electro | Dendro | CATALYZE | 双方subGauge(min(g,bg)) | addElement(trigger); add Catalyze | R | 347-361 |
| Cryo | Hydro | FREEZE | aura.subGauge(g); trigger.subGauge(bg) | add Frozen(min(g,bg)*2) | R | 367-371 |
| Cryo | Pyro | MELT | aura.subGauge(g*0.5) | — | A(弱) | 372-375 |
| Cryo | Electro | SUPERCONDUCT | — | addElement(trigger) | R | 376-379 |
| Cryo | Dendro | 无反应 | — | addElement(trigger) | N | 380-382 |
| Dendro | Hydro | BLOOM | — | — | R(空壳) | 385-387 |
| Dendro | Pyro | COMBUSTION | — | addElement(trigger); 注册燃烧 | R | 388-399 |
| Dendro | Electro | CATALYZE | 双方subGauge(min(g,bg)) | addElement(trigger); add Catalyze | R | 400-415 |
| Anemo | *(任一) | SWIRL | — | — | R | 417-419 |
| Geo | *(任一) | CRYSTALLIZE | — | — | R(空壳) | 420-422 |

### 增幅倍率（AMPLIFIED outcome 的 multiplier）

对应 `AmpReaction.getReactionBaseMul`（AmpReaction.java:21-46），multiplier = `1 + baseMul`：
- VAPORIZE：Hydro打Pyro（first=Pyro,second=Hydro）→ baseMul=1，gauge系数*2（强）
  ；Pyro打Hydro（first=Hydro,second=Pyro）→ baseMul=0.5，gauge系数*0.5（弱）
- MELT：Pyro打Cryo（first=Cryo,second=Pyro）→ baseMul=1，gauge系数*2（强）
  ；Cryo打Pyro（first=Pyro,second=Cryo）→ baseMul=0.5，gauge系数*0.5（弱）

⚠️ 注意：getReactionBaseMul 的 first/second 由 Reaction.create 的参数顺序决定
（旧代码 Reaction.create(..., bRElement, element) 即 first=aura, second=trigger）。
AMPLIFIED outcome 须同时携带 multiplier 与对应 gauge 扣减，两者来自旧代码不同位置，
迁移时方向核对一致。

### 不对称/需特别注意的点

1. **感电两路径判定条件不同**：
   - Hydro×Electro（旧:266）要求 `attackerElementContainer != null` 才注册
   - Electro×Hydro（旧:333）用 `!isElectroCharged` 判断
   - 迁移到同一 EC handler 时需决定统一条件（推荐：都用 reactions.containsKey 判断）
2. **OVERLOAD 不对称**：Electro打Pyro 移除 aura，Pyro打Electro 不移除（用户确认有意，保留）
3. **Hydro×Electro 旧代码 `else if (bRElement instanceof Element)`**：是 catch-all 兜底
   （注释写"雷"但匹配所有），依赖前面分支拦截。查表后该兜底消失，
   Electro 通过显式 HYDRO×ELECTRO 表项命中。
4. **冻结不再返回 null**：旧代码 Hydro×Cryo(253-259)、Cryo×Hydro(367-371) 不 return
   （走方法末尾 null）。新方案返回 REACTION_OCCURRED。
5. **增幅倍率两套硬编码**：getReactionBaseMul 返回 1/0.5（caller 用 1+baseMul），
   gauge 系数 2/0.5 是另一套。AMPLIFIED outcome 须同时携带 multiplier 与对应 gauge 扣减，
   迁移时方向核对一致。
6. **冻结 reaction 类型**：旧 ReactionType 无 FREEZE。本次冻结 outcome 为 REACTION_OCCURRED，
   不进 Mixin 增幅分支，副作用（生成 Frozen）已在 handler 完成，故不强制需要 FREEZE 枚举。

## 风险点

1. **递归触发（最高风险）**：剧变效果挪进结算阶段后，handler 内 `owner.damage(...)` 会再次进 Mixin
   → 再调 resolveReaction。需确保反应自身伤害源（setCannotApply/DamageKind）让二次调用跳过结算。
   解耦后最易出 bug 处。
2. **逐组合迁移**：13 类组合 gauge 系数/移除/类型必须逐字节对照旧实现，否则反应数值偏移。
3. **ReactionOutcome 三态覆盖**：必须覆盖所有现有路径，否则漏算/误算伤害。
4. **`/element add` 改调**：命令不再产生 0.1 伤害记录，行为变化需可接受。
5. **签名变更波及**：applyElement 返回 ReactionOutcome，LivingEntityMixin 调用方须同步改。
6. **扩散传染链**：Swirl 靠伤害源 setElement + 不设 setCannotApply 实现，handler 内保留这两个设置。

## 验证标准

- `.\gradlew.bat build --console=plain` 通过（Windows）
- DEBUG 模式手动回归：阶段 G 的 8 类反应数值与重构前一致（尤其 gauge 扣减方向）
- `/element add` 不再造成任何伤害即可附着
- 持续反应（感电/燃烧）只注册一次，重进世界不恢复（已知设计）
- 增幅反应（蒸发/融化）正确改写 amount 且只乘一次

## 与发布文档的关系

本 Plan 是 `docs/release-discussions/2026-06-25-discussion-01-release-plan.md` 的前置子集：
- 共享：ElementApplicationService（非伤害入口）的设计思想
- 本次不做的发布文档内容：附魔系统、AttackEntityCallback、玩家攻击改走非伤害入口、六元素玩法试玩版
- 完成 Plan 后，发布文档剩余工作变为"给 ElementApplicationService 接上附魔+AttackEntityCallback 触发源"
