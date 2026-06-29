# 元素反应规则记录

本文档记录已经在当前实现中明确下来的元素反应规则，供后续实现、回归和调参使用。

## 元素附着轮询

元素攻击命中目标时，以本次攻击元素作为 `trigger`，目标身上的附着元素作为 `aura`。

轮询流程：

1. 按 `trigger` 对应的 aura 优先级选择一个目标元素。
2. 用 `trigger + aura` 查询 `ReactionTable`。
3. 找到反应后，将当前上下文交给反应结算。
4. 反应内部完成元素消耗、伤害倍率或加算、生成新元素、持续反应注册和反应小字。
5. 反应退出后，继续从当前元素列表选择下一个 aura。
6. 选择次数达到 `10`、选不到 aura、或查不到反应时退出。

aura 选择不会因为 `ReactionTable` 中没有对应反应而跳过该元素。若选中的高优先级 aura 查不到反应，本次轮询直接退出，不能继续选择低优先级 aura。

当前 aura 选择顺序：

```text
HYDRO:    PYRO, CRYO, FROZEN, ELECTRO, QUICKEN, DENDRO
PYRO:     FROZEN, CRYO, HYDRO, ELECTRO, QUICKEN, DENDRO
ELECTRO:  PYRO, HYDRO, FROZEN, CRYO, DENDRO
CRYO:     HYDRO, PYRO, ELECTRO
DENDRO:   HYDRO, PYRO, ELECTRO
ANEMO:    PYRO, HYDRO, ELECTRO, CRYO, FROZEN
GEO:      PYRO, HYDRO, ELECTRO, CRYO, FROZEN
```

超激化、蔓激化不走 `ReactionTable` 普通轮询。雷 / 草元素命中时会先检查目标在本次命中前是否已经存在 `Catalyze`，若存在则先结算对应的超激化 / 蔓激化加成，然后再进入普通元素反应轮询。

`Catalyze` 在普通轮询中会被雷 / 草触发元素忽略，因此 `Electro + QUICKEN` 和 `DENDRO + QUICKEN` 不再作为 `ReactionTable` 条目。由本次雷草反应新生成的 `Catalyze` 不会在同一次命中中立刻触发超激化或蔓激化。

## 元素附着衰减

### 普通元素

普通元素使用 `Element` 的默认匀速衰减：

```text
每 tick 衰减量 = 元素量 / (7 + 2.5 * 元素量) / 20
```

### Frozen / 冻元素

冻元素不使用普通元素的匀速衰减。冻元素会越掉越快：

- 初始衰减速度：`0.4 单位/秒`
- 每过 `1 秒`，衰减速度增加 `0.1 单位/秒`
- Minecraft 按 `20 tick/秒` 计算

换算到 tick：

```text
已存在秒数 = ageTicks / 20
当前每秒衰减速度 = 0.4 + 0.1 * 已存在秒数
每 tick 衰减量 = 当前每秒衰减速度 / 20
```

例如：

- 第 0-1 秒：`0.4 单位/秒`
- 第 1-2 秒：`0.5 单位/秒`
- 第 2-3 秒：`0.6 单位/秒`

当冻元素被重新生成或刷新元素量时，冻元素的衰减计时从 `0` 重新开始。

### Catalyze / 激元素

激元素使用按初始元素量计算的匀速衰减。

持续时间：

```text
持续时间 = 5 * 初始元素量 + 6 秒
```

衰减速率：

```text
每秒衰减量 = 初始元素量 / 持续时间
每 tick 衰减量 = 每秒衰减量 / 20
```

例如 `1` 单位激元素：

```text
持续时间 = 5 * 1 + 6 = 11 秒
每秒衰减量 = 1 / 11 ≈ 0.091
每 tick 衰减量 ≈ 0.00455
```

## 冻结反应

冻结由 `Hydro + Cryo` 或 `Cryo + Hydro` 生成冻元素。

冻元素生成量：

```text
冻元素量 = min(触发元素量, 附着元素量) * 2
```

冻结反应会按参与反应的元素量消耗 Hydro 和 Cryo，并在目标上附着 `Frozen`。

## 激化系列

### Quicken / 原激化

`Dendro + Electro` 或 `Electro + Dendro` 生成激元素，当前实现中对应 `Catalyze`。

原激化生成会生成等量激元素：

```text
激元素量 = min(触发元素量, 附着元素量)
```

### Aggravate / 超激化

雷元素攻击处于原激化状态的目标时触发超激化。

- 反应类型归类为 `CATALYZE`
- 不消耗激元素
- 不消耗本次雷元素
- 本次雷元素可继续参与其它 aura 的反应；若没有触发普通消耗反应，可继续附着到目标上，因此目标可以进入激雷共存状态
- 额外伤害为加算固定值，不是倍率增伤

额外伤害：

```text
额外伤害 = 5 * 1.15 * (1 + (5 * EM) / (EM + 1200))
```

### Spread / 蔓激化

草元素攻击处于原激化状态的目标时触发蔓激化。

- 反应类型归类为 `CATALYZE`
- 不消耗激元素
- 不消耗本次草元素
- 本次草元素可继续参与其它 aura 的反应
- 额外伤害为加算固定值，不是倍率增伤

额外伤害：

```text
额外伤害 = 5 * 1.25 * (1 + (5 * EM) / (EM + 1200))
```

## 激元素作为草系状态

激元素在多数反应关系中按草系状态处理，但不能和草元素或雷元素发生普通消耗反应。

- `Hydro + Quicken` 按 `Hydro + Dendro` 的绽放路径处理。
- `Pyro + Quicken` 按 `Pyro + Dendro` 的燃烧路径处理。
- `Electro + Quicken` 不进入普通 `ReactionTable` 查询；若目标在命中前已有 Quicken，则在普通轮询前触发超激化加成。
- `Dendro + Quicken` 不进入普通 `ReactionTable` 查询；若目标在命中前已有 Quicken，则在普通轮询前触发蔓激化加成。
- `Cryo + Quicken` 不反应。
- `Anemo / Geo` 不扩散或结晶草系状态。

当前绽放首轮仍不展开完整草原核逻辑。

## 持续反应

### Electro-Charged / 感电

感电是持续反应，应绑定到实际附着在目标身上的 Hydro 与 Electro 元素实例。

触发感电后不应因为触发瞬间的统一消耗逻辑提前移除参与元素，否则持续伤害会失效。

### Burning / 燃烧

燃烧是持续反应，应绑定到实际附着在目标身上的 Pyro 与 Dendro 元素实例。

触发燃烧后不应因为触发瞬间的统一消耗逻辑提前移除参与元素，否则持续伤害会失效。

## Superconduct / 超导

超导由 `Cryo + Electro`、`Electro + Cryo`，或 Electro 与冻元素相关路径触发。

超导应消耗等量的冰元素和雷元素：

```text
消耗量 = min(冰元素量, 雷元素量)
```

这里的冰元素可以是 `Cryo`，也可以是相关路径中的 `Frozen`。
