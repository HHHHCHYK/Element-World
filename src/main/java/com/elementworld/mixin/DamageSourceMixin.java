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
    private boolean isNormalReaction = false;

    @Unique
    public Element elementWorld$getElement() {
        return damageElementType;
    }

    @Unique
    public DamageSource elementWorld$setDamageElementType(Element element){
        this.damageElementType = element;
        return (DamageSource) (Object)(this);
    }

    @Override
    public boolean elementWorld$isNormalReaction() {
        return isNormalReaction;
    }

    @Override
    public DamageSource elementWorld$setNormal(){
        isNormalReaction = true;
        return (DamageSource) (Object) this;
    }


}
