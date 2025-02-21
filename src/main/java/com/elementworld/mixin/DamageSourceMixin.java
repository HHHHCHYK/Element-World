package com.elementworld.mixin;


import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceHolder {

    @Unique @Nullable
    private Element damageElementType = null;

    @Unique
    public Element elementWorld$getElement() {
        return damageElementType;
    }

    @Unique
    public void elementWorld$setDamageElementType(Element element){
        this.damageElementType = element;
    }


}
