package com.elementworld.mixin.livingEntity;

import com.elementworld.ElementContainer;
import com.elementworld.elementComponents.EWDamageSource;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityHolder {

    @Shadow public abstract Vec3d applyMovementInput(Vec3d movementInput, float slipperiness);

    @Shadow protected abstract void attackLivingEntity(LivingEntity target);

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
    public ElementContainer getElementContainer$EW() {
        //懒加载
        if(elementContainer == null){
            elementContainer = new ElementContainer((LivingEntity) (Object)this);
        }
        return elementContainer;
    }


    //更改伤害判定
    @Inject(method = "applyDamage",at = @At("HEAD"))
    public void applyDamageMixin(DamageSource source, float amount, CallbackInfo ci){
        System.out.println("ApplyDamageMixin");
        LivingEntity thisLivingEntity = (LivingEntity) (Object) this;//获取当前生物的实例
        ElementContainer thisContainer = ((LivingEntityHolder) thisLivingEntity).getElementContainer$EW();

        if(!thisLivingEntity.isInvulnerableTo(source)){//判断生物是否对于来源无敌
            if(source instanceof DamageSourceHolder damageHolder){
                EWDamageSource ewDamageSource = damageHolder.getEWDamageSource$EW();
                Element element = ewDamageSource.getDamageElement();


                switch (ewDamageSource.getDamageType()){
                    case AmpReaction -> {
                    }
                }
            }

        }
    }
}
