package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public abstract class Reaction {
    private static final double BASE_REACTION_DAMAGE = 0.6;
    private static final ReactionType[] AMPLIFYING_REACTIONS = {ReactionType.VAPORIZE, ReactionType.MELT};
    private static final ReactionType[] TRANSFORMATIVE_REACTIONS = {
            ReactionType.OVERLOAD,
            ReactionType.ELECTRO_CHARGED,
            ReactionType.SUPERCONDUCT,
            ReactionType.SHATTER,
            ReactionType.COMBUSTION,
            ReactionType.SWIRL
    };
    private static final ReactionType[] BLOOM_REACTIONS = {
            ReactionType.BLOOM,
            ReactionType.BURGEON,
            ReactionType.HYPERBLOOM
    };
    private static final ReactionType[] OTHER_REACTIONS = {ReactionType.CRYSTALLIZE, ReactionType.CATALYZE};

    protected final LivingEntity owner;
    protected final LivingEntity attacker;
    protected final Element firstElement;
    protected final Element secondElement;
    protected final float damageValue;
    protected ReactionType reactionType;
    protected DamageSource damageSource;

    public Reaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        this.reactionType = reactionType;
        this.owner = owner;
        this.damageSource = damageSource;
        attacker = damageSource != null && damageSource.getAttacker() instanceof LivingEntity livingAttacker ? livingAttacker : null;

        double masteryBonus = secondElement.getOwnerContainer() != null
                ? secondElement.getOwnerContainer().getMasteryBonus()
                : 0;
        damageValue = (float) (BASE_REACTION_DAMAGE * (1 + masteryBonus));

        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    public Reaction(LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        this(null, owner, damageSource, firstElement, secondElement);
    }

    public enum ReactionType {
        VAPORIZE,
        MELT,
        OVERLOAD,
        ELECTRO_CHARGED,
        SUPERCONDUCT,
        SHATTER,
        COMBUSTION,
        SWIRL,
        BLOOM,
        BURGEON,
        HYPERBLOOM,
        CRYSTALLIZE,
        CATALYZE
    }

    public void apply() {
    }

    public static ReactionType[] getReactionGroup(ReactionType type) {
        return switch (type) {
            case CATALYZE, CRYSTALLIZE -> OTHER_REACTIONS;
            case COMBUSTION, SWIRL, OVERLOAD, ELECTRO_CHARGED, SUPERCONDUCT, SHATTER -> TRANSFORMATIVE_REACTIONS;
            case HYPERBLOOM, BLOOM, BURGEON -> BLOOM_REACTIONS;
            case MELT, VAPORIZE -> AMPLIFYING_REACTIONS;
        };
    }

    public static Reaction create(
            ReactionType reactionType,
            LivingEntity owner,
            DamageSource damageSource,
            Element firstElement,
            Element secondElement
    ) {
        return switch (reactionType) {
            case CATALYZE -> new Catalyze(reactionType, owner, damageSource, firstElement, secondElement);
            case CRYSTALLIZE -> new Crystallize(owner, damageSource, firstElement, secondElement);
            case OVERLOAD, SUPERCONDUCT, SHATTER -> new TFReaction(reactionType, owner, damageSource, firstElement, secondElement);
            case SWIRL -> new Swirl(owner, damageSource, firstElement, secondElement);
            case HYPERBLOOM, BLOOM, BURGEON -> new BloomReaction(reactionType, owner, damageSource, firstElement, secondElement);
            case MELT, VAPORIZE -> new AmpReaction(reactionType, owner, damageSource, firstElement, secondElement);
            case COMBUSTION -> new Combustion(owner, damageSource, firstElement, secondElement);
            case ELECTRO_CHARGED -> new ElectroCharged(owner, damageSource, firstElement, secondElement);
        };
    }
}
