package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Catalyze;
import com.elementworld.elements.Dendro;
import com.elementworld.elements.Element;
import com.elementworld.elements.Frozen;
import net.minecraft.entity.damage.DamageSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 反应查表。以「后手元素类型 × 附着元素类型」为 key 查找 {@link ReactionHandler}。
 *
 * <p>所有 gauge 系数、元素增删、反应类型与重构前 {@code ElementContainer.applyElement} 的
 * instanceof 分支逐字节一致（见 {@code reaction-refactor-plan.md} 的迁移对照表）。
 * 剧变 / 持续反应的副作用在 handler 内立即执行；伤害侧只读 {@link ReactionOutcome}。
 */
public final class ReactionTable {

    /** 元素附着损耗系数（{@code ElementContainer.addElement} 的 0.8）。 */
    public static final double ATTACH_DECAY = 0.8;

    /** 增幅反应 gauge 强系数（有利方向，gauge*2）。 */
    public static final double STRONG_MUL = 2.0;

    /** 增幅反应 gauge 弱系数（不利方向，gauge*0.5）。 */
    public static final double WEAK_MUL = 0.5;

    private static final Map<Element.ElementType, Map<Element.ElementType, ReactionHandler>> TABLE = new HashMap<>();

    static {
        // ============ 后手 Hydro ============
        // Hydro×Pyro: 蒸发（强）— aura.subGauge(g*2)
        register(Element.ElementType.HYDRO, Element.ElementType.PYRO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge() * STRONG_MUL);
            return amplified(ctx, true);
        });

        // Hydro×Cryo: 冻结 — add Frozen(min(g,bg)*2)，双方扣 gauge，副作用完成，非增幅
        register(Element.ElementType.HYDRO, Element.ElementType.CRYO, ctx -> {
            double gauge = ctx.trigger().getGauge();
            double beGauge = ctx.aura().getGauge();
            ctx.ownerContainer().addFrozenElement(new Frozen(Math.min(gauge, beGauge) * 2));
            ctx.aura().subGauge(gauge);
            ctx.trigger().subGauge(ctx.aura().getGauge());
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Hydro×Frozen: 冻元素被水打 — 仅 addElement，无反应
        register(Element.ElementType.HYDRO, Element.ElementType.FROZEN, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return ReactionOutcome.NONE;
        });

        // Hydro×Electro: 感电 — addElement(trigger)，注册 EC（统一 containsKey 去重）
        register(Element.ElementType.HYDRO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return registerElectroCharged(ctx)
                    ? ReactionOutcome.REACTION_OCCURRED
                    : ReactionOutcome.NONE;
        });

        // Hydro×Dendro: 绽放（空壳）— aura.subGauge(g)，副作用完成
        register(Element.ElementType.HYDRO, Element.ElementType.DENDRO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge());
            Reaction.create(Reaction.ReactionType.BLOOM, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // ============ 后手 Pyro ============
        // Pyro×Hydro: 蒸发（弱）— aura.subGauge(g*0.5)
        register(Element.ElementType.PYRO, Element.ElementType.HYDRO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge() * WEAK_MUL);
            return amplified(ctx, false);
        });

        // Pyro×Electro: 超载 — aura.subGauge(g)，不移除（不对称保留），副作用 apply()
        register(Element.ElementType.PYRO, Element.ElementType.ELECTRO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge());
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Pyro×Cryo: 融化（强）— aura.subGauge(g*2)
        register(Element.ElementType.PYRO, Element.ElementType.CRYO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge() * STRONG_MUL);
            return amplified(ctx, true);
        });

        // Pyro×Frozen: 融化（强）— aura.subGauge(g*2)
        register(Element.ElementType.PYRO, Element.ElementType.FROZEN, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge() * STRONG_MUL);
            return amplified(ctx, true);
        });

        // Pyro×Dendro: 燃烧 — addElement(trigger)，注册燃烧
        register(Element.ElementType.PYRO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return registerCombustion(ctx)
                    ? ReactionOutcome.REACTION_OCCURRED
                    : ReactionOutcome.NONE;
        });

        // ============ 后手 Electro ============
        // Electro×Pyro: 超载（不对称，移除 aura）— removeElement(aura)，副作用 apply()
        register(Element.ElementType.ELECTRO, Element.ElementType.PYRO, ctx -> {
            ctx.ownerContainer().removeAuraElement(ctx.aura());
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Electro×Hydro: 感电 — addElement(trigger)，注册 EC（统一 containsKey 去重）
        register(Element.ElementType.ELECTRO, Element.ElementType.HYDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return registerElectroCharged(ctx)
                    ? ReactionOutcome.REACTION_OCCURRED
                    : ReactionOutcome.NONE;
        });

        // Electro×Cryo / Electro×Frozen: 超导 — addElement(trigger)
        register(Element.ElementType.ELECTRO, Element.ElementType.CRYO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.FROZEN, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Electro×Dendro: 激化 — addElement(trigger)，双方 subGauge(min)，生成激元素
        register(Element.ElementType.ELECTRO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // ============ 后手 Cryo ============
        // Cryo×Hydro: 冻结 — 双方扣 gauge，add Frozen(min*2)
        register(Element.ElementType.CRYO, Element.ElementType.HYDRO, ctx -> {
            double gauge = ctx.trigger().getGauge();
            double beGauge = ctx.aura().getGauge();
            ctx.aura().subGauge(gauge);
            ctx.trigger().subGauge(beGauge);
            ctx.ownerContainer().addFrozenElement(new Frozen(Math.min(gauge, beGauge) * 2));
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Cryo×Pyro: 融化（弱）— aura.subGauge(g*0.5)
        register(Element.ElementType.CRYO, Element.ElementType.PYRO, ctx -> {
            ctx.aura().subGauge(ctx.trigger().getGauge() * WEAK_MUL);
            return amplified(ctx, false);
        });

        // Cryo×Electro: 超导 — addElement(trigger)
        register(Element.ElementType.CRYO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Cryo×Dendro: 不反应 — 仅 addElement(trigger)
        register(Element.ElementType.CRYO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return ReactionOutcome.NONE;
        });

        // ============ 后手 Dendro ============
        // Dendro×Hydro: 绽放（空壳）— 无 gauge 变化
        register(Element.ElementType.DENDRO, Element.ElementType.HYDRO, ctx -> {
            Reaction.create(Reaction.ReactionType.BLOOM, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // Dendro×Pyro: 燃烧 — addElement(trigger)，注册燃烧
        register(Element.ElementType.DENDRO, Element.ElementType.PYRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return registerCombustion(ctx)
                    ? ReactionOutcome.REACTION_OCCURRED
                    : ReactionOutcome.NONE;
        });

        // Dendro×Electro: 激化 — addElement(trigger)，双方 subGauge(min)，生成激元素
        register(Element.ElementType.DENDRO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });

        // ============ 后手 Anemo / Geo ============
        // Anemo×(任意): 扩散 — 副作用 apply()（传染链：Swirl 内 setElement + 不设 cannotApply）
        register(Element.ElementType.ANEMO, Element.ElementType.PYRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.HYDRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.CRYO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.ELECTRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.DENDRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.FROZEN, swirl());

        // Geo×(任意): 结晶（空壳）— 仅 create，apply 为空
        register(Element.ElementType.GEO, Element.ElementType.PYRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.HYDRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.CRYO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.ELECTRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.DENDRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.FROZEN, crystallize());
    }

    private ReactionTable() {
    }

    /**
     * 查询反应处理器。
     *
     * @param trigger 后手元素类型
     * @param aura    附着元素类型
     * @return 命中的 handler，或 {@code null}（无该组合）
     */
    public static @org.jetbrains.annotations.Nullable ReactionHandler get(Element.ElementType trigger, Element.ElementType aura) {
        Map<Element.ElementType, ReactionHandler> row = TABLE.get(trigger);
        return row == null ? null : row.get(aura);
    }

    private static void register(Element.ElementType trigger, Element.ElementType aura, ReactionHandler handler) {
        TABLE.computeIfAbsent(trigger, t -> new HashMap<>()).put(aura, handler);
    }

    // ==================== handler 工具 ====================

    /**
     * 构造增幅反应 outcome。
     * <p>原 caller 用 {@code (1 + getReactionBaseMul())}：强 baseMul=1 → 2.0，弱 baseMul=0.5 → 1.5。
     * 精通乘数取自攻击者容器（无攻击者容器时为 1）。
     *
     * @param strong true=强（有利方向），false=弱
     */
    private static ReactionOutcome.Amplified amplified(ReactionContext ctx, boolean strong) {
        double masteryMul = ctx.attackerContainer() != null
                ? AmpReaction.getMasteryAmp(ctx.attackerContainer().getMastery())
                : 1.0;
        return strong ? ReactionOutcome.Amplified.strong(masteryMul) : ReactionOutcome.Amplified.weak(masteryMul);
    }

    /**
     * 注册感电持续反应（统一 containsKey 去重，消除旧代码两路径判定不一致）。
     *
     * @return 本次确实新注册了感电返回 true，已存在返回 false
     */
    private static boolean registerElectroCharged(ReactionContext ctx) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(ElectroCharged.class)) {
            return false;
        }
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.ELECTRO_CHARGED, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        reactions.put(ElectroCharged.class, reaction);
        container.markElectroCharged();
        return true;
    }

    /**
     * 注册燃烧持续反应（统一 containsKey 去重）。
     *
     * @return 本次确实新注册了燃烧返回 true，已存在返回 false
     */
    private static boolean registerCombustion(ReactionContext ctx) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(Combustion.class)) {
            return false;
        }
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.COMBUSTION, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        reactions.put(Combustion.class, reaction);
        container.markCombustion();
        return true;
    }

    /**
     * 激化 gauge 扣减 + 激元素生成（消除原 applyElement 两处重复）。
     * 双方 subGauge(min(g,bg))，并 add 激元素（Catalyze，QUICKEN）。
     * source != null 时 source 用 DamageSource.getSource，否则用 owner。
     */
    private static void applyCatalyze(ReactionContext ctx) {
        double min = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
        ctx.aura().subGauge(min);
        ctx.trigger().subGauge(min);
        Object sourceEntity = ctx.source() != null ? ctx.source().getSource() : ctx.owner();
        ctx.ownerContainer().addCatalyzeElement(new Catalyze(min, ctx.owner(), ctx.attacker(), (net.minecraft.entity.Entity) sourceEntity));
    }

    /** 冻结公式：{@code min(g,bg)*2}。 */
    public static double freezeFormula(double triggerGauge, double auraGauge) {
        return Math.min(triggerGauge, auraGauge) * 2;
    }

    private static ReactionHandler swirl() {
        return ctx -> {
            Reaction.create(Reaction.ReactionType.SWIRL, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        };
    }

    private static ReactionHandler crystallize() {
        return ctx -> {
            Reaction.create(Reaction.ReactionType.CRYSTALLIZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        };
    }
}
