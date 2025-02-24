package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Quicken extends Dendro{
    public Quicken(double gauge) {
        super(gauge);
        decaySpeed = (5*gauge+6)/20;
    }

    public Quicken(double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
        decaySpeed = (5*gauge+6)/20;
    }

    @Override
    public String toString() {
        return "Quicken";
    }
}
