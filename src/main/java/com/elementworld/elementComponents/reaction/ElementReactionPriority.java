package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Selects which existing aura a trigger element should react with first.
 */
public final class ElementReactionPriority {
    private static final Map<Element.ElementType, List<Element.ElementType>> PRIORITIES =
            new EnumMap<>(Element.ElementType.class);

    static {
        PRIORITIES.put(Element.ElementType.HYDRO, List.of(
                Element.ElementType.PYRO,
                Element.ElementType.CRYO,
                Element.ElementType.FROZEN,
                Element.ElementType.ELECTRO,
                Element.ElementType.QUICKEN,
                Element.ElementType.DENDRO
        ));
        PRIORITIES.put(Element.ElementType.PYRO, List.of(
                Element.ElementType.FROZEN,
                Element.ElementType.CRYO,
                Element.ElementType.HYDRO,
                Element.ElementType.ELECTRO,
                Element.ElementType.QUICKEN,
                Element.ElementType.DENDRO
        ));
        PRIORITIES.put(Element.ElementType.ELECTRO, List.of(
                Element.ElementType.QUICKEN,
                Element.ElementType.PYRO,
                Element.ElementType.HYDRO,
                Element.ElementType.FROZEN,
                Element.ElementType.CRYO,
                Element.ElementType.DENDRO
        ));
        PRIORITIES.put(Element.ElementType.CRYO, List.of(
                Element.ElementType.HYDRO,
                Element.ElementType.PYRO,
                Element.ElementType.ELECTRO
        ));
        PRIORITIES.put(Element.ElementType.DENDRO, List.of(
                Element.ElementType.QUICKEN,
                Element.ElementType.HYDRO,
                Element.ElementType.PYRO,
                Element.ElementType.ELECTRO
        ));
        PRIORITIES.put(Element.ElementType.ANEMO, List.of(
                Element.ElementType.PYRO,
                Element.ElementType.HYDRO,
                Element.ElementType.ELECTRO,
                Element.ElementType.CRYO,
                Element.ElementType.FROZEN
        ));
        PRIORITIES.put(Element.ElementType.GEO, List.of(
                Element.ElementType.PYRO,
                Element.ElementType.HYDRO,
                Element.ElementType.ELECTRO,
                Element.ElementType.CRYO,
                Element.ElementType.FROZEN
        ));
    }

    private ElementReactionPriority() {
    }

    public static @Nullable Element selectAura(
            Element.ElementType triggerType,
            Collection<Element> elements,
            Set<Element> handledAuras
    ) {
        List<Element.ElementType> priority = PRIORITIES.get(triggerType);
        if (priority == null) {
            return null;
        }
        for (Element.ElementType auraType : priority) {
            for (Element element : elements) {
                if (!handledAuras.contains(element)
                        && Element.typeOfElementClass(element.getClass()) == auraType
                        && element.getGauge() > 0) {
                    return element;
                }
            }
        }
        return null;
    }
}
