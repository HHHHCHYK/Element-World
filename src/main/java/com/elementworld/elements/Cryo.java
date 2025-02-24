package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Cryo extends Element{
    public Cryo(double gauge) {
        super(gauge);
    }
    public Cryo (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public String toString() {
        return "Cryo";
    }
}

