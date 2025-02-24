package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Pyro extends Element{
    public Pyro(double gauge) {
        super(gauge);
    }

    public Pyro (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public String toString() {
        return "Pyro";
    }
}
