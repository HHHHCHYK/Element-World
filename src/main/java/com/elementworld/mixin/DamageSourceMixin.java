package com.elementworld.mixin;

import com.elementworld.elementComponents.EWDamageSource;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceHolder {
    @Unique
    private EWDamageSource ewDamageSource = new EWDamageSource((DamageSource) (Object) this);

    @Unique
    @Override
    @Nullable
    public Element getElement$EW() {
        return getEWDamageSource$EW().getDamageElement();
    }

    @Unique
    @Override
    public void setDamageElement$EW(@Nullable Element element) {
        getEWDamageSource$EW().setElement(element);
    }

    @Unique
    @Override
    public void setDamageKind$EW(EWDamageSource.DamageKind damageKind) {
        getEWDamageSource$EW().setDamageKind(damageKind);
    }

    @Unique
    @Override
    public EWDamageSource getEWDamageSource$EW() {
        if (ewDamageSource == null) {
            ewDamageSource = new EWDamageSource((DamageSource) (Object) this);
        }
        return ewDamageSource;
    }
}
