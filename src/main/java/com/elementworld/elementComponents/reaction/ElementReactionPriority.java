package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Selects which existing aura a trigger element should react with first.
 */
public final class ElementReactionPriority {
    private static final List<Element.ElementType> SHARED_PRIORITY = List.of(
            Element.ElementType.QUICKEN,
            Element.ElementType.FROZEN,
            Element.ElementType.PYRO,
            Element.ElementType.HYDRO,
            Element.ElementType.ELECTRO,
            Element.ElementType.CRYO,
            Element.ElementType.DENDRO
    );

    private static final Map<Element.ElementType, List<Element.ElementType>> PRIORITIES =
            new EnumMap<>(Element.ElementType.class);

    static {
        for (Element.ElementType type : Element.ElementType.values()) {
            PRIORITIES.put(type, SHARED_PRIORITY);
        }
    }

    private ElementReactionPriority() {
    }

    public static @Nullable Element selectAura(Element.ElementType triggerType, Collection<Element> elements) {
        List<Element.ElementType> priority = PRIORITIES.getOrDefault(triggerType, SHARED_PRIORITY);
        for (Element.ElementType auraType : priority) {
            if (ReactionTable.get(triggerType, auraType) == null) {
                continue;
            }
            for (Element element : elements) {
                if (Element.typeOfElementClass(element.getClass()) == auraType && element.getGauge() > 0) {
                    return element;
                }
            }
        }
        return null;
    }
}
