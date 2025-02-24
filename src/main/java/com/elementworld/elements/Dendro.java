package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Dendro extends Element{
    public Dendro(double gauge) {
        super(gauge);
    }

    public Dendro (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }


    @Override
    public String toString() {
        return "Dendro";
    }
}

