package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Catalyze;
import com.elementworld.elements.Element;
import com.elementworld.elements.Frozen;
import com.elementworld.floatingtext.FloatingTextService;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Reaction lookup table keyed by trigger element and existing aura element.
 */
public final class ReactionTable {
    public static final double ATTACH_DECAY = 0.8;
    public static final double STRONG_MUL = 2.0;
    public static final double WEAK_MUL = 0.5;
    public static final double CATALYZE_LEVEL_MULTIPLIER = 5.0;
    public static final double AGGRAVATE_MUL = 1.15;
    public static final double SPREAD_MUL = 1.25;
    private static final String VAPORIZE_KEY = "reaction.elementworld.vaporize";
    private static final String MELT_KEY = "reaction.elementworld.melt";
    private static final String OVERLOAD_KEY = "reaction.elementworld.overload";
    private static final String ELECTRO_CHARGED_KEY = "reaction.elementworld.electro_charged";
    private static final String SUPERCONDUCT_KEY = "reaction.elementworld.superconduct";
    private static final String COMBUSTION_KEY = "reaction.elementworld.combustion";
    private static final String SWIRL_KEY = "reaction.elementworld.swirl";
    private static final String BLOOM_KEY = "reaction.elementworld.bloom";
    private static final String CRYSTALLIZE_KEY = "reaction.elementworld.crystallize";
    private static final String FREEZE_KEY = "reaction.elementworld.freeze";
    private static final String QUICKEN_KEY = "reaction.elementworld.quicken";
    private static final String AGGRAVATE_KEY = "reaction.elementworld.aggravate";
    private static final String SPREAD_KEY = "reaction.elementworld.spread";

    private static final Map<Element.ElementType, Map<Element.ElementType, ReactionHandler>> TABLE = new HashMap<>();

    static {
        register(Element.ElementType.HYDRO, Element.ElementType.PYRO, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true, VAPORIZE_KEY);
        });
        register(Element.ElementType.HYDRO, Element.ElementType.CRYO, ctx -> {
            freeze(ctx);
            return occurred(ctx, FREEZE_KEY);
        });
        register(Element.ElementType.HYDRO, Element.ElementType.ELECTRO, ctx -> {
            Element triggerAttachment = ctx.ownerContainer().addIncomingElement(ctx.trigger());
            registerElectroCharged(ctx, triggerAttachment);
            consumeTrigger(ctx);
            return occurred(ctx, ELECTRO_CHARGED_KEY);
        });
        register(Element.ElementType.HYDRO, Element.ElementType.DENDRO, ctx -> {
            applyBloom(ctx);
            return occurred(ctx, BLOOM_KEY);
        });
        register(Element.ElementType.HYDRO, Element.ElementType.QUICKEN, ctx -> {
            applyBloom(ctx);
            return occurred(ctx, BLOOM_KEY);
        });

        register(Element.ElementType.PYRO, Element.ElementType.HYDRO, ctx -> {
            consumeAuraByTrigger(ctx, WEAK_MUL);
            return amplified(ctx, false, VAPORIZE_KEY);
        });
        register(Element.ElementType.PYRO, Element.ElementType.ELECTRO, ctx -> {
            consumeAuraByTrigger(ctx, 1.0);
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return occurred(ctx, OVERLOAD_KEY);
        });
        register(Element.ElementType.PYRO, Element.ElementType.CRYO, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true, MELT_KEY);
        });
        register(Element.ElementType.PYRO, Element.ElementType.FROZEN, ctx -> {
            consumeAuraByTrigger(ctx, STRONG_MUL);
            return amplified(ctx, true, MELT_KEY);
        });
        register(Element.ElementType.PYRO, Element.ElementType.DENDRO, ctx -> {
            Element triggerAttachment = ctx.ownerContainer().addIncomingElement(ctx.trigger());
            registerCombustion(ctx, triggerAttachment);
            consumeTrigger(ctx);
            return occurred(ctx, COMBUSTION_KEY);
        });

        register(Element.ElementType.ELECTRO, Element.ElementType.PYRO, ctx -> {
            double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
            ctx.trigger().subGauge(used);
            ctx.ownerContainer().removeAuraElement(ctx.aura());
            Reaction.create(Reaction.ReactionType.OVERLOAD, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            return occurred(ctx, OVERLOAD_KEY);
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.HYDRO, ctx -> {
            Element triggerAttachment = ctx.ownerContainer().addIncomingElement(ctx.trigger());
            registerElectroCharged(ctx, triggerAttachment);
            consumeTrigger(ctx);
            return occurred(ctx, ELECTRO_CHARGED_KEY);
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.CRYO, ctx -> {
            applySuperconduct(ctx);
            return occurred(ctx, SUPERCONDUCT_KEY);
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.FROZEN, ctx -> {
            applySuperconduct(ctx);
            return occurred(ctx, SUPERCONDUCT_KEY);
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.DENDRO, ctx -> {
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return occurred(ctx, QUICKEN_KEY);
        });
        register(Element.ElementType.ELECTRO, Element.ElementType.QUICKEN, ctx ->
                catalyzeAdditive(ctx, AGGRAVATE_MUL, "Aggravate", AGGRAVATE_KEY));

        register(Element.ElementType.CRYO, Element.ElementType.HYDRO, ctx -> {
            freeze(ctx);
            return occurred(ctx, FREEZE_KEY);
        });
        register(Element.ElementType.CRYO, Element.ElementType.PYRO, ctx -> {
            consumeAuraByTrigger(ctx, WEAK_MUL);
            return amplified(ctx, false, MELT_KEY);
        });
        register(Element.ElementType.CRYO, Element.ElementType.ELECTRO, ctx -> {
            applySuperconduct(ctx);
            return occurred(ctx, SUPERCONDUCT_KEY);
        });

        register(Element.ElementType.DENDRO, Element.ElementType.HYDRO, ctx -> {
            applyBloom(ctx);
            return occurred(ctx, BLOOM_KEY);
        });
        register(Element.ElementType.DENDRO, Element.ElementType.PYRO, ctx -> {
            Element triggerAttachment = ctx.ownerContainer().addIncomingElement(ctx.trigger());
            registerCombustion(ctx, triggerAttachment);
            consumeTrigger(ctx);
            return occurred(ctx, COMBUSTION_KEY);
        });
        register(Element.ElementType.DENDRO, Element.ElementType.ELECTRO, ctx -> {
            applyCatalyze(ctx);
            Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            return occurred(ctx, QUICKEN_KEY);
        });
        register(Element.ElementType.DENDRO, Element.ElementType.QUICKEN, ctx ->
                catalyzeAdditive(ctx, SPREAD_MUL, "Spread", SPREAD_KEY));

        register(Element.ElementType.ANEMO, Element.ElementType.PYRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.HYDRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.CRYO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.ELECTRO, swirl());
        register(Element.ElementType.ANEMO, Element.ElementType.FROZEN, swirl());

        register(Element.ElementType.GEO, Element.ElementType.PYRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.HYDRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.CRYO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.ELECTRO, crystallize());
        register(Element.ElementType.GEO, Element.ElementType.FROZEN, crystallize());

        register(Element.ElementType.PYRO, Element.ElementType.QUICKEN, ctx -> {
            Element triggerAttachment = ctx.ownerContainer().addIncomingElement(ctx.trigger());
            registerCombustion(ctx, triggerAttachment);
            consumeTrigger(ctx);
            return occurred(ctx, COMBUSTION_KEY);
        });
    }

    private ReactionTable() {
    }

    public static @org.jetbrains.annotations.Nullable ReactionHandler get(Element.ElementType trigger, Element.ElementType aura) {
        Map<Element.ElementType, ReactionHandler> row = TABLE.get(trigger);
        return row == null ? null : row.get(aura);
    }

    public static boolean isNonConsuming(Element.ElementType trigger, Element.ElementType aura) {
        return (trigger == Element.ElementType.ELECTRO || trigger == Element.ElementType.DENDRO)
                && aura == Element.ElementType.QUICKEN;
    }

    private static void register(Element.ElementType trigger, Element.ElementType aura, ReactionHandler handler) {
        TABLE.computeIfAbsent(trigger, t -> new HashMap<>()).put(aura, handler);
    }

    private static ReactionOutcome amplified(ReactionContext ctx, boolean strong, String translationKey) {
        double masteryMul = ctx.attackerContainer() != null
                ? AmpReaction.getMasteryAmp(ctx.attackerContainer().getMastery())
                : 1.0;
        spawnFeedback(ctx, translationKey);
        ReactionOutcome.Amplified current = strong
                ? ReactionOutcome.Amplified.strong(masteryMul)
                : ReactionOutcome.Amplified.weak(masteryMul);
        return mergeOutcome(ctx.previousOutcome(), current);
    }

    private static ReactionOutcome occurred(ReactionContext ctx, String translationKey) {
        spawnFeedback(ctx, translationKey);
        return mergeOutcome(ctx.previousOutcome(), ReactionOutcome.REACTION_OCCURRED);
    }

    private static void spawnFeedback(ReactionContext ctx, String translationKey) {
        FloatingTextService.spawnReactionLabel(
                ctx.owner(),
                Text.translatable(translationKey),
                triggerType(ctx)
        );
    }

    private static Element.ElementType triggerType(ReactionContext ctx) {
        Element.ElementType triggerType = Element.typeOfElementClass(ctx.trigger().getClass());
        return triggerType == null ? Element.ElementType.PHYSICS : triggerType;
    }

    private static void registerElectroCharged(ReactionContext ctx, Element triggerAttachment) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(ElectroCharged.class)) {
            return;
        }
        Element reactionTrigger = triggerAttachment != null ? triggerAttachment : ctx.trigger();
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.ELECTRO_CHARGED, ctx.owner(), ctx.source(), ctx.aura(), reactionTrigger);
        reactions.put(ElectroCharged.class, reaction);
        container.markElectroCharged();
    }

    private static void registerCombustion(ReactionContext ctx, Element triggerAttachment) {
        ElementContainer container = ctx.ownerContainer();
        HashMap<Class<? extends Reaction>, Reaction> reactions = container.ensureReactionsMap();
        if (reactions.containsKey(Combustion.class)) {
            return;
        }
        Element reactionTrigger = triggerAttachment != null ? triggerAttachment : ctx.trigger();
        Reaction reaction = Reaction.create(
                Reaction.ReactionType.COMBUSTION, ctx.owner(), ctx.source(), ctx.aura(), reactionTrigger);
        reactions.put(Combustion.class, reaction);
        container.markCombustion();
    }

    private static void applyCatalyze(ReactionContext ctx) {
        double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
        ctx.trigger().subGauge(used);
        ctx.aura().subGauge(used);
        if (used <= 0) {
            return;
        }
        Object sourceEntity = ctx.source() != null ? ctx.source().getSource() : ctx.owner();
        ctx.ownerContainer().addCatalyzeElement(new Catalyze(
                used, ctx.owner(), ctx.attacker(), (net.minecraft.entity.Entity) sourceEntity));
    }

    private static void applySuperconduct(ReactionContext ctx) {
        double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
        if (used <= 0) {
            return;
        }
        ctx.trigger().subGauge(used);
        ctx.aura().subGauge(used);
        if (ctx.trigger().getGauge() > 0) {
            ctx.ownerContainer().addIncomingElement(ctx.trigger());
            consumeTrigger(ctx);
        }
        Reaction.create(Reaction.ReactionType.SUPERCONDUCT, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
    }

    private static ReactionOutcome catalyzeAdditive(
            ReactionContext ctx,
            double reactionCoefficient,
            String label,
            String translationKey
    ) {
        // Aggravate / Spread are additive reactions and do not consume any element.
        Reaction.create(Reaction.ReactionType.CATALYZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
        spawnFeedback(ctx, translationKey);
        ReactionOutcome.Additive current = new ReactionOutcome.Additive(
                Reaction.ReactionType.CATALYZE,
                label,
                catalyzeBonusDamage(ctx, reactionCoefficient)
        );
        return mergeOutcome(ctx.previousOutcome(), current);
    }

    private static double catalyzeBonusDamage(ReactionContext ctx, double reactionCoefficient) {
        double mastery = ctx.attackerContainer() != null ? ctx.attackerContainer().getMastery() : 0.0;
        return CATALYZE_LEVEL_MULTIPLIER
                * reactionCoefficient
                * (1.0 + (5.0 * mastery) / (mastery + 1200.0));
    }

    public static double freezeFormula(double triggerGauge, double auraGauge) {
        return Math.min(triggerGauge, auraGauge) * 2;
    }

    private static void freeze(ReactionContext ctx) {
        double frozenGauge = freezeFormula(ctx.trigger().getGauge(), ctx.aura().getGauge());
        if (frozenGauge <= 0) {
            return;
        }
        double used = frozenGauge / 2.0;
        ctx.trigger().subGauge(used);
        ctx.aura().subGauge(used);
        ctx.ownerContainer().addFrozenElement(new Frozen(frozenGauge));
    }

    private static void applyBloom(ReactionContext ctx) {
        double used = Math.min(ctx.trigger().getGauge(), ctx.aura().getGauge());
        ctx.trigger().subGauge(used);
        ctx.aura().subGauge(used);
        Reaction.create(Reaction.ReactionType.BLOOM, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
    }

    private static void consumeTrigger(ReactionContext ctx) {
        ctx.trigger().subGauge(ctx.trigger().getGauge());
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
            Reaction.create(Reaction.ReactionType.SWIRL, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger()).apply();
            consumeTrigger(ctx);
            return occurred(ctx, SWIRL_KEY);
        };
    }

    private static ReactionHandler crystallize() {
        return ctx -> {
            Reaction.create(Reaction.ReactionType.CRYSTALLIZE, ctx.owner(), ctx.source(), ctx.aura(), ctx.trigger());
            consumeTrigger(ctx);
            return occurred(ctx, CRYSTALLIZE_KEY);
        };
    }

    private static ReactionOutcome mergeOutcome(ReactionOutcome previous, ReactionOutcome current) {
        if (previous instanceof ReactionOutcome.Amplified) {
            return previous;
        }
        if (current instanceof ReactionOutcome.Amplified) {
            return current;
        }
        if (previous instanceof ReactionOutcome.Additive) {
            return previous;
        }
        if (current instanceof ReactionOutcome.Additive) {
            return current;
        }
        if (previous == ReactionOutcome.NONE) {
            return current;
        }
        return previous;
    }
}
