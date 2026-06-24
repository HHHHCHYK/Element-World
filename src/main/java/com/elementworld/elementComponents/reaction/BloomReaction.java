package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class BloomReaction extends Reaction{

    public BloomReaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(reactionType, owner, damageSource, firstElement, secondElement);
    }
}
