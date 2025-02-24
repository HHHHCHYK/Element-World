package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Frozen extends Element{

    public Frozen(double gauge){
        super(gauge);
        decaySpeed = (Math.sqrt(5*gauge+4)*2-4) / 20;
    }

    public Frozen(double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
        decaySpeed = (Math.sqrt(5*gauge+4)*2-4) / 20;
    }

    @Override
    public String toString() {
        return "Frozen";
    }
}
