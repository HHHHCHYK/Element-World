package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Hydro extends Element{
    public Hydro (double gauge){
        super(gauge);
    }

    public Hydro (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public String toString() {
        return "Hydro";
    }
}
