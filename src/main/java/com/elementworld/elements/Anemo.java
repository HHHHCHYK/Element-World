package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Anemo extends Element{
    public Anemo(double gauge) {
        super(gauge);
    }

    public Anemo (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public String toString() {
        return "Anemo";
    }
}
