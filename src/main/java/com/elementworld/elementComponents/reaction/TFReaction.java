package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class TFReaction extends Reaction{

    public TFReaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement) {
        super(reactionType, owner, damageSource, firseElement, secondELement);
    }

    public void apply(){

    }

}
