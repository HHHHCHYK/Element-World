package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class Catalyze extends Reaction{

    public Catalyze(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement) {
        super(reactionType, owner, damageSource, firseElement, secondELement);
    }
}
