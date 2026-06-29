package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Catalyze extends Dendro{
    private static final int TICKS_PER_SECOND = 20;

    public Catalyze(double gauge) {
        super(gauge);
        decaySpeed = decaySpeedFor(gauge);
    }

    public Catalyze(double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
        decaySpeed = decaySpeedFor(gauge);
    }

    private static double decaySpeedFor(double gauge) {
        double durationSeconds = 5 * gauge + 6;
        return gauge / durationSeconds / TICKS_PER_SECOND;
    }

    @Override
    public String toString() {
        return "Catalyze";
    }
}
