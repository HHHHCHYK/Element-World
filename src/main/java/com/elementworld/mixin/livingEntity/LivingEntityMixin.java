package com.elementworld.mixin.livingEntity;

import com.elementworld.ElementContainer;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityHolder {
    @Shadow @Final public LimbAnimator limbAnimator;
    @Unique
    public ElementContainer elementContainer;

    @Inject(method = "tick",at = @At("HEAD"))
    public void livingEntityTickMixin(CallbackInfo info){
        //懒加载
        if(elementContainer == null){
            elementContainer = new ElementContainer((LivingEntity) (Object)this);
        }
        this.elementContainer.tick();
    }

    @Override
    public ElementContainer elementWorld$getElementContainer() {
        //懒加载
        if(elementContainer == null){
            elementContainer = new ElementContainer((LivingEntity) (Object)this);
        }
        return elementContainer;
    }
}
