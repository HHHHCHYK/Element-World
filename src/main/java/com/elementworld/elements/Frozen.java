package com.elementworld.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Frozen extends Element{
    private static final int TICKS_PER_SECOND = 20;
    private static final double INITIAL_DECAY_PER_SECOND = 0.4;
    private static final double DECAY_INCREASE_PER_SECOND = 0.1;

    private int ageTicks;

    public Frozen(double gauge){
        super(gauge);
    }

    public Frozen(double gauge, LivingEntity owner, LivingEntity attacker, Entity source){
        super(gauge,owner,attacker,source);
    }

    @Override
    public void tick() {
        int elapsedSeconds = ageTicks / TICKS_PER_SECOND;
        double decayPerSecond = INITIAL_DECAY_PER_SECOND + DECAY_INCREASE_PER_SECOND * elapsedSeconds;
        gauge -= decayPerSecond / TICKS_PER_SECOND;
        ageTicks++;
    }

    @Override
    public void setGauge(double gauge) {
        super.setGauge(gauge);
        ageTicks = 0;
    }

    @Override
    public String toString() {
        return "Frozen";
    }
}
