package com.elementworld.persistence;

import com.elementworld.elementComponents.modifiers.Modifier;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elements.Element;
import com.elementworld.elements.EP;

import java.util.List;

public record ElementContainerState(
        List<ElementEntry> elements,
        double mastery,
        boolean displayElement,
        double shieldStrength,
        List<Class<? extends EP>> immuneElements,
        List<BonusEntry> bonuses,
        List<ResistanceEntry> resistances,
        List<ModifierGroup> modifiers,
        List<ShieldEntry> shields
) {
    public record ElementEntry(Element.ElementType type, double gauge) {
    }

    public record BonusEntry(Class<? extends EP> elementType, double value, String name) {
    }

    public record ResistanceEntry(Class<? extends EP> elementType, double value, String name) {
    }

    public record ModifierGroup(Modifiers.ModifierType type, List<ModifierEntry> entries) {
    }

    public record ModifierEntry(String name, double value, int duration, Modifier.ModifierMethod method) {
    }

    public record ShieldEntry(String name, double value, double maxValue, Class<? extends Element> elementType) {
    }
}
