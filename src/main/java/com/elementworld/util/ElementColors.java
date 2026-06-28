package com.elementworld.util;

import com.elementworld.elements.Element;
import org.jetbrains.annotations.Nullable;

public final class ElementColors {
    public static final int PHYSICS = 0xFFE8E8E8;
    public static final int DEFAULT = 0xFFFFFFFF;

    private ElementColors() {
    }

    public static int colorFor(@Nullable Element.ElementType elementType) {
        if (elementType == null) {
            return PHYSICS;
        }

        return switch (elementType) {
            case ANEMO -> 0xFF63E6BE;
            case GEO -> 0xFFE3B341;
            case ELECTRO -> 0xFFB783FF;
            case HYDRO -> 0xFF48C6FF;
            case PYRO -> 0xFFFF5A3C;
            case CRYO -> 0xFF9DEBFF;
            case DENDRO -> 0xFF6BD65A;
            case FROZEN -> 0xFFB7F4FF;
            case QUICKEN -> 0xFFB6F15F;
            case PHYSICS -> PHYSICS;
        };
    }

    public static int colorForName(String elementName) {
        return switch (elementName) {
            case "Anemo" -> colorFor(Element.ElementType.ANEMO);
            case "Geo" -> colorFor(Element.ElementType.GEO);
            case "Electro" -> colorFor(Element.ElementType.ELECTRO);
            case "Hydro" -> colorFor(Element.ElementType.HYDRO);
            case "Pyro" -> colorFor(Element.ElementType.PYRO);
            case "Cryo" -> colorFor(Element.ElementType.CRYO);
            case "Dendro" -> colorFor(Element.ElementType.DENDRO);
            case "Frozen" -> colorFor(Element.ElementType.FROZEN);
            case "Catalyze", "Quicken" -> colorFor(Element.ElementType.QUICKEN);
            case "Physics" -> PHYSICS;
            default -> DEFAULT;
        };
    }
}