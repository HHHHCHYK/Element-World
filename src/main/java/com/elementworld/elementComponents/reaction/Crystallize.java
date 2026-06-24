package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class Crystallize extends Reaction{
    public Crystallize(LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(owner, damageSource, firstElement, secondElement);
    }
}
