package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Electro extends Element{
    public Electro(double gauge) {
        super(gauge);
    }

    public Electro (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public String toString() {
        return "Electro";
    }
}
