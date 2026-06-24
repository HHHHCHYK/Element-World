package com.elementworld.elementComponents.modifiers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Modifier {
    public enum ModifierMethod {
        ADD,
        MULTI
    }

    private final String name;
    private final double value;
    private final ModifierMethod method;
    private final UUID modifierUUID;
    private int duration;
    private boolean die = false;

    public Modifier(String name, double value, int duration, ModifierMethod method) {
        this.name = name;
        this.method = method;
        this.value = value;
        this.duration = duration;
        modifierUUID = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    public void tick() {
        if (duration > 0) {
            duration--;
        } else {
            die = true;
        }
    }

    public float apply(float baseAmount) {
        return switch (method) {
            case ADD -> baseAmount + (float) value;
            case MULTI -> baseAmount * (float) value;
        };
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public ModifierMethod getMethod() {
        return method;
    }

    public int getDuration() {
        return duration;
    }

    public UUID getModifierUUID() {
        return modifierUUID;
    }

    public boolean isDie() {
        return die;
    }
}
