package com.elementworld.environment;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;

public final class EnvironmentElementService {
    private static final int ENVIRONMENT_APPLY_INTERVAL_TICKS = 5;
    private static final double PYRO_GAUGE = 1.0;
    private static final double WATER_GAUGE = 1.0;

    private EnvironmentElementService() {
    }

    public static void tick(LivingEntity entity, ElementContainer container) {
        if (entity.getWorld().isClient() || !entity.isAlive()) {
            return;
        }
        if ((entity.age + entity.getId()) % ENVIRONMENT_APPLY_INTERVAL_TICKS != 0) {
            return;
        }

        applyWaterAttachment(entity, container);
        applyFireAttachment(entity, container);
    }

    private static void applyWaterAttachment(LivingEntity entity, ElementContainer container) {
        if (!entity.isTouchingWater()) {
            return;
        }

        applyEnvironmentElement(container, Element.ElementType.HYDRO, WATER_GAUGE);
    }

    private static void applyFireAttachment(LivingEntity entity, ElementContainer container) {
        if (!entity.isOnFire() && !entity.isInLava()) {
            return;
        }

        applyEnvironmentElement(container, Element.ElementType.PYRO, PYRO_GAUGE);
    }

    private static void applyEnvironmentElement(
            ElementContainer container,
            Element.ElementType elementType,
            double gauge
    ) {
        Element element = Element.create(elementType, gauge);
        if (element != null) {
            container.resolveReaction(element, null, null);
        }
    }
}