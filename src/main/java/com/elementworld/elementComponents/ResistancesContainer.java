package com.elementworld.elementComponents;

import com.elementworld.ElementWorld;
import com.elementworld.elements.Anemo;
import com.elementworld.elements.Cryo;
import com.elementworld.elements.Dendro;
import com.elementworld.elements.EP;
import com.elementworld.elements.Electro;
import com.elementworld.elements.Geo;
import com.elementworld.elements.Hydro;
import com.elementworld.elements.Physics;
import com.elementworld.elements.Pyro;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ResistancesContainer {
    public record ResistanceInstance(@Nullable Class<? extends EP> elementType, double resistanceValue, String name) {
    }

    private static class ResistanceSet {
        private final Map<String, ResistanceInstance> resistanceInstances = new HashMap<>();
        private final Class<? extends EP> elementType;

        private ResistanceSet(Class<? extends EP> elementType) {
            this.elementType = elementType;
        }

        private void addResistance(double value, String name) {
            if (resistanceInstances.containsKey(name)) {
                ElementWorld.LOGGER.warn("Resistance instance already exists: {}", name);
                return;
            }
            resistanceInstances.put(name, new ResistanceInstance(elementType, value, name));
        }

        private boolean removeResistance(String name) {
            return resistanceInstances.remove(name) != null;
        }

        private double apply() {
            return resistanceInstances.values().stream().mapToDouble(ResistanceInstance::resistanceValue).sum();
        }
    }

    private final LivingEntity owner;
    private final Map<Class<? extends EP>, ResistanceSet> resistanceSets = new HashMap<>();

    public ResistancesContainer(LivingEntity owner) {
        this.owner = owner;
    }

    public void addResistance(@Nullable Class<? extends EP> elementType, double value, String name) {
        getResistanceSet(elementType).addResistance(value, name);
    }

    public boolean removeResistance(@Nullable Class<? extends EP> elementType, String name) {
        return getResistanceSet(elementType).removeResistance(name);
    }

    public double getResistanceValue(@Nullable Class<? extends EP> elementType) {
        return getResistanceSet(elementType).apply();
    }

    public LivingEntity getOwner() {
        return owner;
    }

    private ResistanceSet getResistanceSet(@Nullable Class<? extends EP> elementType) {
        Class<? extends EP> normalizedElementType = normalize(elementType);
        return resistanceSets.computeIfAbsent(normalizedElementType, ResistanceSet::new);
    }

    private static Class<? extends EP> normalize(@Nullable Class<? extends EP> elementType) {
        if (elementType == Hydro.class || elementType == Pyro.class || elementType == Anemo.class
                || elementType == Cryo.class || elementType == Dendro.class || elementType == Electro.class
                || elementType == Geo.class) {
            return elementType;
        }
        return Physics.class;
    }
}
