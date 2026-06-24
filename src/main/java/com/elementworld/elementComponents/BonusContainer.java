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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonusContainer {
    public record BonusInstance(@Nullable Class<? extends EP> elementType, double bonusValue, String name) {
    }

    private static class BonusSet {
        private final Map<String, BonusInstance> bonusInstances = new HashMap<>();
        private final Class<? extends EP> elementType;

        private BonusSet(Class<? extends EP> elementType) {
            this.elementType = elementType;
        }

        private void addBonus(double value, String name) {
            if (bonusInstances.containsKey(name)) {
                ElementWorld.LOGGER.warn("Bonus instance already exists: {}", name);
                return;
            }
            bonusInstances.put(name, new BonusInstance(elementType, value, name));
        }

        private boolean removeBonus(String name) {
            return bonusInstances.remove(name) != null;
        }

        private boolean hasBonusInstance(String name) {
            return bonusInstances.containsKey(name);
        }

        private double apply() {
            return bonusInstances.values().stream().mapToDouble(BonusInstance::bonusValue).sum();
        }
    }

    private final LivingEntity owner;
    private final Map<Class<? extends EP>, BonusSet> bonusSets = new HashMap<>();

    public BonusContainer(LivingEntity owner) {
        this.owner = owner;
    }

    public void addBonus(Class<? extends EP> elementType, double value, String name) {
        getBonusSet(elementType).addBonus(value, name);
    }

    public boolean removeBonus(Class<? extends EP> elementType, String name) {
        return getBonusSet(elementType).removeBonus(name);
    }

    public boolean hasBonus(String name, Class<? extends EP> elementType) {
        return getBonusSet(elementType).hasBonusInstance(name);
    }

    public double getBonusValue(Class<? extends EP> elementType) {
        return getBonusSet(elementType).apply();
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public List<BonusInstance> getBonusInstances() {
        List<BonusInstance> instances = new ArrayList<>();
        for (BonusSet bonusSet : bonusSets.values()) {
            instances.addAll(bonusSet.bonusInstances.values());
        }
        return instances;
    }

    public void clear() {
        bonusSets.clear();
    }

    private BonusSet getBonusSet(Class<? extends EP> elementType) {
        Class<? extends EP> normalizedElementType = normalize(elementType);
        return bonusSets.computeIfAbsent(normalizedElementType, BonusSet::new);
    }

    private static Class<? extends EP> normalize(Class<? extends EP> elementType) {
        if (elementType == Hydro.class || elementType == Pyro.class || elementType == Anemo.class
                || elementType == Cryo.class || elementType == Dendro.class || elementType == Electro.class
                || elementType == Geo.class) {
            return elementType;
        }
        return Physics.class;
    }
}
