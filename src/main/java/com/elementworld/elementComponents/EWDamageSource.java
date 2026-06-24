package com.elementworld.elementComponents;

import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import com.elementworld.elements.Physics;
import com.elementworld.interfaces.DamageSourceHolder;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EWDamageSource {
    public enum DamageKind {
        AMPLIFYING_REACTION,
        TRANSFORMATIVE_REACTION,
        DOT,
        PHYSICAL
    }

    @NotNull
    private final DamageSource originDamageSource;

    @Nullable
    private final DamageSourceHolder damageSourceHolder;

    @Nullable
    private Element damageElement;
    private DamageKind damageKind = DamageKind.AMPLIFYING_REACTION;
    private boolean cannotApply = false;

    public EWDamageSource(@NotNull DamageSource damageSource) {
        originDamageSource = damageSource;
        damageSourceHolder = damageSource instanceof DamageSourceHolder holder ? holder : null;
    }

    @Nullable
    public DamageSourceHolder getDamageSourceHolder() {
        return damageSourceHolder;
    }

    public void setElement(@Nullable Element element) {
        damageElement = element;
    }

    @Nullable
    public Element getDamageElement() {
        return damageElement;
    }

    @NotNull
    public DamageSource getDamageSource() {
        return originDamageSource;
    }

    public DamageKind getDamageKind() {
        return damageKind;
    }

    public void setDamageKind(DamageKind damageKind) {
        this.damageKind = damageKind;
    }

    public Class<? extends EP> getEP() {
        return damageElement == null ? Physics.class : damageElement.getClass();
    }

    public boolean isCannotApply() {
        return cannotApply;
    }

    public void setCannotApply() {
        cannotApply = true;
    }

    public void resetCannotApply() {
        cannotApply = false;
    }
}
