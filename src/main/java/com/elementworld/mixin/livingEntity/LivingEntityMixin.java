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
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityHolder {

    @Shadow
    public abstract Vec3d applyMovementInput(Vec3d movementInput, float slipperiness);

    @Shadow
    protected abstract void attackLivingEntity(LivingEntity target);

    @Shadow
    public abstract void damageHelmet(DamageSource source, float amount);

    @Shadow
    public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow
    public abstract void remove(Entity.RemovalReason reason);

    @Shadow
    public abstract boolean removeStatusEffect(StatusEffect type);

    @Unique
    public ElementContainer elementContainer;


    @Inject(method = "tick", at = @At("HEAD"))
    public void livingEntityTickMixin(CallbackInfo info) {
        //懒加载
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        this.elementContainer.tick();
    }

    @Unique
    @Override
    public ElementContainer getElementContainer$EW() {
        //懒加载
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        return elementContainer;
    }


    //更改伤害判定
    @ModifyArgs(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            )
    )
    public void applyDamageModifyArgs(Args args) {
        DamageSource source = args.get(0);
        float amount = args.get(1);
        //((LivingEntity) (Object) this).sendMessage(Text.literal("1"));//Debug
        LivingEntity thisLivingEntity = (LivingEntity) (Object) this;//获取当前生物的实例


        if (amount <= 0) {
            return;
        }

        //thisLivingEntity.sendMessage(Text.literal("2"));//Debug
        if (source != null) {
            /*
             * 1.获取攻击者的元素容器
             */
            //(thisLivingEntity).sendMessage(Text.literal("3"));

            if (thisLivingEntity == null) {//空值检查
                //((LivingEntity) (Object) this).sendMessage(Text.literal("This is null"));
                return;
            }
            ElementContainer ownContainer = ((LivingEntityHolder) this).getElementContainer$EW();//获取自身元素容器

            if (!thisLivingEntity.isInvulnerableTo(source)) {//判断生物是否对于来源无敌
                //(thisLivingEntity).sendMessage(Text.literal("Not Invulnerable"));//Debug
                if (source instanceof DamageSourceHolder damageHolder) {
                    if (damageHolder.getEWDamageSource$EW() == null) {//空值检查
                        return;
                    }
                    if (!ownContainer.isImmune(damageHolder.getEWDamageSource$EW().getEP())) {//判断生物是否有对某个元素的免疫
                        //(thisLivingEntity).sendMessage(Text.literal("Not Immune"));//Debug
                        Element element = damageHolder.getElement$EW();
                        if (element != null) {//如果这次攻击带有元素附着
                            //(thisLivingEntity).sendMessage(Text.literal("Has Element"));//Debug

                            Reaction reaction;
                            if (!damageHolder.getEWDamageSource$EW().isCannotApply()) {//判断这次反应是否造成元素附着
                                //(thisLivingEntity).sendMessage(Text.literal("can Apply"));//Debug
                                /*
                                这一步为元素反应容器添加附着
                                 */
                                reaction = ownContainer.applyElement(element, source);//添加元素，获得反应实例
                                //(thisLivingEntity).sendMessage(Text.literal("Has Reaction"));//Debug


                                if (source.getAttacker() != null) {//存在攻击者
                                    ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                    if (attackerContainer != null) {
                                        if (reaction != null) {//如果产生了元素反应


                                            if (reaction instanceof AmpReaction ampReaction) {//如果反应类型为增幅反应
                                                amount = (float) (amount
                                                        * (1 + (ampReaction.getReactionBaseMul()))//元素反应增伤
                                                        * AmpReaction.getMasteryAmp(attackerContainer.getMastery())//精通增伤
                                                        * (1 + attackerContainer.getBonusValue(Element.ToEP(element.getClass())))//增伤乘区
                                                        * (1 - ownContainer.getResistanceValue(Element.ToEP(element.getClass())))//减抗乘区
                                                );
                                            } else {
                                                reaction.apply();//运行一次元素反应内容
                                            }
                                        } else {//如果没有产生元素反应
                                            //thisLivingEntity.sendMessage(Text.literal("No Reaction"));
                                            Class<? extends EP> elementType = element.getClass();
                                            amount = (float) (amount
                                                    * (1 + attackerContainer.getBonusValue(elementType))
                                                    * (1 - ownContainer.getResistanceValue(elementType))
                                            );
                                        }
                                    }
                                } else {//如果攻击者为空
                                    //thisLivingEntity.sendMessage(Text.literal("Attacker is null"));
                                    if (reaction != null) {//如果产生了元素反应


                                        if (reaction instanceof AmpReaction ampReaction) {//如果反应类型为增幅反应
                                            amount = (float) (amount
                                                    * (1 + (ampReaction.getReactionBaseMul()))//元素反应增伤
                                                    * (1 - ownContainer.getResistanceValue(Element.ToEP(element.getClass())))//减抗乘区
                                            );
                                        } else {
                                            reaction.apply();//运行一次元素反应内容
                                        }
                                    } else {//如果没有产生元素反应
                                        //thisLivingEntity.sendMessage(Text.literal("No Reaction"));
                                        Class<? extends EP> elementType = element.getClass();
                                        amount = (float) (amount
                                                * (1 - ownContainer.getResistanceValue(elementType))
                                        );
                                    }
                                }
                            } else {//如果不造成元素附着
                                amount = (float) (amount
                                        * (1 - ownContainer.getResistanceValue(Element.ToEP(element.getClass())))
                                );
                                /*
                                如果存在攻击者，则计算增伤乘区
                                 */
                                if (source.getAttacker() != null) {//如果存在攻击者
                                    ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                    if (attackerContainer != null) {
                                        amount = (float) (amount
                                                * (1 + attackerContainer.getBonusValue(Element.ToEP(element.getClass())))
                                        );
                                    }
                                }
                            }

                        } else {//这次攻击不附带元素附着
                            //thisLivingEntity.sendMessage(Text.literal("No Element"));
                            if (source.getAttacker() != null) {//存在攻击者

                                ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                if (attackerContainer != null) {//空值检查
                                    amount = (float) (amount
                                            * (1 + attackerContainer.getBonusValue(Physics.class))
                                            * (1 - ownContainer.getResistanceValue(Physics.class))
                                    );
                                } else {//不存在攻击者
                                    //thisLivingEntity.sendMessage(Text.literal("Attacker is null"));
                                    amount = (float) (amount
                                            * (1 - ownContainer.getResistanceValue(Physics.class))
                                    );
                                }

                            }
                        }
                    }

                    /*
                    下面处理护盾对于伤害的影响
                        对于damageHolder的非空检查：source ！= null
                     */
                    //((LivingEntity) (Object) this).sendMessage(Text.literal("11111"));//Debug
                    Element element = damageHolder.getElement$EW();
                    //下面对于element进行非空检查，如若空则直接返回null，非空则返回其类
                    if (element == null) {
                        amount = ownContainer.applyShield(null, amount);
                    } else {
                        amount = ownContainer.applyShield(element.getClass(), amount);
                    }
                }
            }
        }
        //((LivingEntity) (Object) this).sendMessage(Text.literal("Finish Modify"));
    }
}


