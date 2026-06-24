package com.elementworld.elementComponents.modifiers;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Modifiers {
    public enum ModifierType {
        MAX_HEALTH,
        BASE_DAMAGE,
        MASTERY,
        RESISTANCE,
        BONUS
    }

    private final ModifierType type;
    private final List<Modifier> modifiers = new ArrayList<>();
    private final HashMap<UUID, Modifier> modifierUUIDHashMap = new HashMap<>();
    private final List<Modifier> deadModifiers = new ArrayList<>();

    public Modifiers(ModifierType target) {
        type = target;
    }

    public void tick() {
        for (Modifier modifier : modifiers) {
            if (modifier.isDie()) {
                deadModifiers.add(modifier);
            }
            modifier.tick();
        }

        modifiers.removeAll(deadModifiers);
        deadModifiers.clear();
    }

    public void addModifier(String name, double value, int duration, Modifier.ModifierMethod method) {
        addModifier(new Modifier(name, value, duration, method));
    }

    public void addModifier(@NotNull Modifier modifier) {
        modifierUUIDHashMap.put(modifier.getModifierUUID(), modifier);
        modifiers.add(modifier);
    }

    public void removeModifier(@NotNull Modifier modifier) {
        modifiers.remove(modifier);
        modifierUUIDHashMap.remove(modifier.getModifierUUID());
    }

    public void removeModifier(String name) {
        Modifier modifier = modifierUUIDHashMap.remove(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        modifiers.remove(modifier);
    }

    public float applyModifiers(float baseAmount) {
        for (Modifier modifier : modifiers) {
            baseAmount = modifier.apply(baseAmount);
        }
        return baseAmount;
    }

    public ModifierType getType() {
        return type;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }
}
