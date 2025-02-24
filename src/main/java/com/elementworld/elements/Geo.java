package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Geo extends Element{
    public Geo(double gauge) {
        super(gauge);
    }
    public Geo (double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }


    @Override
    public String toString() {
        return "Geo";
    }
}
