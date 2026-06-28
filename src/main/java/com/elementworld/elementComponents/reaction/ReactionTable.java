package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Catalyze;
import com.elementworld.elements.Element;
import com.elementworld.elements.Frozen;
import com.elementworld.floatingtext.FloatingTextService;
import com.elementworld.util.ElementColors;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Reaction lookup table keyed by trigger element and existing aura element.
 */
public final class ReactionTable {
    public static final double ATTACH_DECAY = 0.8;
    public static final double STRONG_MUL = 2.0;
    public static final double WEAK_MUL = 0.5;
    public static final double CATALYZE_LEVEL_MULTIPLIER = 1664.0;
    public static final double AGGRAVATE_MUL = 1.15;
    public static final double SPREAD_MUL = 1.25;

    private static final Map<Element.ElementType, Map<Element.ElementType, ReactionHandler>> TABLE = new HashMap<>();

    static {
        register(Element.ElementType.HYDRO, Element.ElementType.PYRO, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true);
        });
        register(Element.ElementType.HYDRO, Element.ElementType.CRYO, ctx -> {
            freeze(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.HYDRO, Element.ElementType.FROZEN, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return ReactionOutcome.NONE;
        });
        register(Element.ElementType.HYDRO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            registerElectroCharged(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.HYDRO, Element.ElementType.DENDRO, ctx -> {
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.BLOOM, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.HYDRO, Element.ElementType.QUICKEN, ctx -> {
            consumeBoth(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });

        register(Element.ElementType.PYRO, Element.ElementType.HYDRO, ctx -> {
            consumeAuraByTrigger(ctx, WEAK_MUL);
            return amplified(ctx, false);
        });
        register(Element.ElementType.PYRO, Element.ElementType.ELECTRO, ctx -> {
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.PYRO, Element.ElementType.CRYO, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true);
        });
        register(Element.ElementType.PYRO, Element.ElementType.FROZEN, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true);
        });
        register(Element.ElementType.PYRO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            registerCombustion(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });

        register(Element.ElementType.ELECTRO, Element.ElementType.PYRO, ctx -> {
            double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
            ctx.trigger().subGauge(used);
            ctx.ownerContainer().removeAuraElement(ctx.aura());
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.HYDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            registerElectroCharged(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.CRYO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.FROZEN, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.QUICKEN, ctx ->
                catalyzeAdditive(ctx, AGGRAVATE_MUL, "Aggravate", Element.ElementType.ELECTRO, true));

        register(Element.ElementType.CRYO, Element.ElementType.HYDRO, ctx -> {
            freeze(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.CRYO, Element.ElementType.PYRO, ctx -> {
            consumeAuraByTrigger(ctx, WEAK_MUL);
            return amplified(ctx, false);
        });
        register(Element.ElementType.CRYO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.CRYO, Element.ElementType.DENDRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            return ReactionOutcome.NONE;
        });

        register(Element.ElementType.DENDRO, Element.ElementType.HYDRO, ctx -> {
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.BLOOM, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.DENDRO, Element.ElementType.PYRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeBoth(ctx);
            registerCombustion(ctx);
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.DENDRO, Element.ElementType.ELECTRO, ctx -> {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        });
        register(Element.ElementType.DENDRO, Element.ElementType.QUICKEN, ctx ->
                catalyzeAdditive(ctx, SPREAD_MUL, "Spread", Element.ElementType.DENDRO, false));

        register(Element.ElementType.ANEMO, Element.ElementType.PYRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.HYDRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.CRYO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.ELECTRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.DENDRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.FROZEN, swirl());

        register(Element.ElementType.GEO, Element.ElementType.PYRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.HYDRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.CRYO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.ELECTRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.DENDRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.FROZEN, crystallize());
    }

    private ReactionTable() {
    }

    public static @org.jetbrains.annotations.Nullable ReactionHandler get(Element.ElementType trigger, Element.ElementType aura) {
        Map<Element.ElementType, ReactionHandler> row = TABLE.get(trigger);
        return row == null ? null : row.get(aura);
    }

    private static void register(Element.ElementType trigger, Element.ElementType aura, ReactionHandler handler) {
        TABLE.computeIfAbsent(trigger, t -> new HashMap<>()).put(aura, handler);
    }

    private static ReactionOutcome.Amplified amplified(ReactionContext ctx, boolean strong) {
        double masteryMul = ctx.attackerContainer() != null
                ? AmpReaction.getMasteryAmp(ctx.attackerContainer().getMastery())
                : 1.0;
        return strong ? ReactionOutcome.Amplified.strong(masteryMul) : ReactionOutcome.Amplified.weak(masteryMul);
    }

    private static void registerElectroCharged(ReactionContext ctx) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(ElectroCharged.class)) {
            return;
        }
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.ELECTRO_CHARGED, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        reactions.put(ElectroCharged.class, reaction);
        container.markElectroCharged();
    }

    private static void registerCombustion(ReactionContext ctx) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(Combustion.class)) {
            return;
        }
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.COMBUSTION, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        reactions.put(Combustion.class, reaction);
        container.markCombustion();
    }

    private static void applyCatalyze(ReactionContext ctx) {
        double used = consumeBoth(ctx);
        if (used <= 0) {
            return;
        }
        Object sourceEntity = ctx.source() != null ? ctx.source().getSource() : ctx.owner();
        ctx.ownerContainer().addCatalyzeElement(new Catalyze(
                used, ctx.owner(), ctx.attacker(), (net.minecraft.entity.Entity) sourceEntity));
        spawnReactionLabel(ctx, "Quicken", Element.ElementType.QUICKEN);
    }

    private static ReactionOutcome.Additive catalyzeAdditive(
            ReactionContext ctx,
            double reactionCoefficient,
            String label,
            Element.ElementType feedbackColor,
            boolean attachTrigger
    ) {
        if (attachTrigger) {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
        }
        ctx.trigger().subGauge(ctx.trigger().getGauge());
        Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        spawnReactionLabel(ctx, label, feedbackColor);
        return new ReactionOutcome.Additive(
                Reaction.ReactionType.CATALYZE,
                label,
                catalyzeBonusDamage(ctx, reactionCoefficient)
        );
    }

    private static double catalyzeBonusDamage(ReactionContext ctx, double reactionCoefficient) {
        double mastery = ctx.attackerContainer() != null ? ctx.attackerContainer().getMastery() : 0.0;
        return CATALYZE_LEVEL_MULTIPLIER
                * reactionCoefficient
                * (1.0 + (5.0 * mastery) / (mastery + 1200.0));
    }

    private static void spawnReactionLabel(
            ReactionContext ctx,
            String label,
            Element.ElementType feedbackColor
    ) {
        FloatingTextService.spawnAroundEntity(
                ctx.owner(),
                label,
                ElementColors.colorFor(feedbackColor),
                FloatingTextService.DEFAULT_LIFETIME_TICKS,
                new Vec3d(0.0D, ctx.owner().getHeight() + 0.7D, 0.0D),
                new Vec3d(0.0D, 0.035D, 0.0D)
        );
    }

    public static double freezeFormula(double triggerGauge, double auraGauge) {
        return Math.min(triggerGauge, auraGauge) * 2;
    }

    private static void freeze(ReactionContext ctx) {
        double used = consumeBoth(ctx);
        if (used > 0) {
            ctx.ownerContainer().addFrozenElement(new Frozen(used * 2));
        }
    }

    private static double consumeBoth(ReactionContext ctx) {
        double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
        ctx.trigger().subGauge(used);
        ctx.aura().subGauge(used);
        return used;
    }

    private static double consumeAuraByTrigger(ReactionContext ctx, double auraPerTrigger) {
        if (auraPerTrigger <= 0) {
            return 0;
        }
        double triggerUsed = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge() / auraPerTrigger);
        ctx.trigger().subGauge(triggerUsed);
        ctx.aura().subGauge(triggerUsed * auraPerTrigger);
        return triggerUsed;
    }

    private static ReactionHandler swirl() {
        return ctx -> {
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.SWIRL, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return ReactionOutcome.REACTION_OCCURRED;
        };
    }

    private static ReactionHandler crystallize() {
        return ctx -> {
            consumeBoth(ctx);
            Reaction.create(Reaction.ReactionType.CRYSTALLIZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return ReactionOutcome.REACTION_OCCURRED;
        };
    }
}
