package com.elementworld.mixin.livingEntity;

import com.elementworld.ElementContainer;
import com.elementworld.elementComponents.reaction.AmpReaction;
import com.elementworld.elementComponents.reaction.Reaction;
import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import com.elementworld.elements.Physics;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityHolder {

    @Shadow public abstract Vec3d applyMovementInput(Vec3d movementInput, float slipperiness);

    @Shadow protected abstract void attackLivingEntity(LivingEntity target);

    @Shadow public abstract void damageHelmet(DamageSource source, float amount);

    @Shadow public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow public abstract void remove(Entity.RemovalReason reason);

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
    @ModifyVariable(
            method = "applyDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    public float applyDamageModifyVariable(float amount,DamageSource source){
        System.out.println("ApplyDamageMixin");
        if(source == null){
            return amount;
        }
        LivingEntity thisLivingEntity = (LivingEntity) (Object) this;//获取当前生物的实例
        ElementContainer ownContainer = ((LivingEntityHolder) thisLivingEntity).getElementContainer$EW();
        ElementContainer attackerContainer = ((LivingEntityHolder) Objects.requireNonNull(source.getAttacker())).getElementContainer$EW();

        if(!thisLivingEntity.isInvulnerableTo(source)){//判断生物是否对于来源无敌
            if(source instanceof DamageSourceHolder damageHolder){
                if(!ownContainer.isImmune(damageHolder.getEWDamageSource$EW().getEP())){//判断生物是否有对某个元素的免疫
                    Element element = damageHolder.getElement$EW();
                    if(!(element == null)){//如果这次攻击带有元素附着
                        Reaction reaction = null ;
                        if(!damageHolder.getEWDamageSource$EW().isCannotApply()){//判断这次反应是否造成元素附着
                             reaction = ownContainer.applyElement(element,source);//添加元素，获得反应实例
                        }
                        if(reaction != null){//如果产生了元素反应
                            if(reaction instanceof AmpReaction ampReaction){//如果反应类型为增幅反应
                                amount = (float) (amount
                                        *(1+(ampReaction.getReactionBaseMul()))//元素反应增伤
                                        *AmpReaction.getMasteryAmp(attackerContainer.getMastery())//精通增伤
                                        *(1+attackerContainer.getBonusValue(Element.ToEP(element.getClass())))//增伤乘区
                                        *(1-ownContainer.getResistanceValue(Element.ToEP(element.getClass())))//减抗乘区
                                );
                            }else{
                                reaction.apply();//运行一次元素反应内容
                            }
                        }else{//如果没有产生元素反应
                            Class<? extends EP> elementType = element.getClass();
                            amount = (float) (amount
                                    *(1+attackerContainer.getBonusValue(elementType))
                                    *(1-ownContainer.getResistanceValue(elementType))
                            );
                        }
                    }else{
                        amount = (float) (amount
                                *(1+attackerContainer.getBonusValue(Physics.class))
                                *(1-ownContainer.getResistanceValue(Physics.class))
                        );
                    }
                }

            }
        }
        return amount;
    }
}
