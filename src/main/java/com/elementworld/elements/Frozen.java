package com.elementworld.elements;

import net.minecraft.entity.LivingEntity;

public class Frozen extends Element{

    public Frozen(double gauge){
        super(gauge);
        decaySpeed = Math.sqrt(5*gauge+4)*2-4;
    }

    @Override
    public String toString() {
        return "Frozen";
    }
}
