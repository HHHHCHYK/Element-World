package com.elementworld.mixin.livingEntity;

import com.elementworld.ElementContainer;
import com.elementworld.elementComponents.reaction.ReactionOutcome;
import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import com.elementworld.elements.Physics;
import com.elementworld.floatingtext.FloatingTextService;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import com.elementworld.persistence.ElementContainerNbtCodec;
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
    private ElementContainer elementContainer;


    @Inject(method = "tick", at = @At("HEAD"))
    public void livingEntityTickMixin(CallbackInfo info) {
        // 懒加载
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        this.elementContainer.tick();
    }

    @Unique
    @Override
    public ElementContainer getElementContainer$EW() {
        // 懒加载
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        return elementContainer;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeElementWorldData(NbtCompound nbt, CallbackInfo info) {
        if (elementContainer != null) {
            ElementContainerNbtCodec.writeToEntityNbt(elementContainer, nbt);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readElementWorldData(NbtCompound nbt, CallbackInfo info) {
        ElementContainerNbtCodec.readFromEntityNbt(getElementContainer$EW(), nbt);
    }


    // 修改伤害判定
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
        LivingEntity thisLivingEntity = (LivingEntity) (Object) this; // 获取当前生物的实例

        if (amount <= 0) {
            return;
        }

        if (source != null) {
            if (thisLivingEntity == null) { // 空值检查
                return;
            }
            ElementContainer ownContainer = ((LivingEntityHolder) this).getElementContainer$EW(); // 获取自身元素容器

            if (!thisLivingEntity.isInvulnerableTo(source)) { // 判断生物是否对于来源无敌
                if (source instanceof DamageSourceHolder damageHolder) {
                    if (damageHolder.getEWDamageSource$EW() == null) { // 空值检查
                        return;
                    }
                    if (!ownContainer.isImmune(damageHolder.getEWDamageSource$EW().getEP())) { // 判断生物是否有对某个元素的免疫
                        Element element = damageHolder.getElement$EW();
                        /*
                         * 反应已与伤害解耦：容器侧 resolveReaction 完成所有反应副作用并返回 outcome。
                         * 伤害侧只读取 outcome 决定倍率：
                         *   - AMPLIFIED：额外乘 reaction 倍率与精通乘数
                         *   - 其他（NONE / REACTION_OCCURRED，含 cannotApply 二级伤害）：不额外改写
                         * 任何「有元素」的攻击（无论是否触发反应）都走增伤×减抗乘区，
                         * 消除旧代码「触发持续反应的攻击漏算乘区」的 BUG。
                         */
                        if (element != null) {
                            Class<? extends EP> elementType = Element.toEpClass(element.getClass());

                            // 解析攻击者及其容器（带 instanceof 检查，避免非生物攻击者抛 CCE）
                            LivingEntity attacker = source.getAttacker() instanceof LivingEntity livingAttacker ? livingAttacker : null;
                            ElementContainer attackerContainer = null;
                            if (attacker instanceof LivingEntityHolder attackerHolder) {
                                attackerContainer = attackerHolder.getElementContainer$EW();
                            }

                            // 结算反应副作用并获取结果（resolveReaction 内部处理 cannotApply 跳过）
                            ReactionOutcome outcome = ownContainer.resolveReaction(element, attacker, source);

                            if (outcome instanceof ReactionOutcome.Amplified amp) {
                                // 增幅反应（蒸发 / 融化）：amount *= 倍率 × 精通 × 增伤 × 减抗
                                amount = (float) (amount * amp.multiplier());
                                if (attackerContainer != null) {
                                    amount = (float) (amount * amp.masteryMultiplier()
                                            * (1 + attackerContainer.getBonusValue(elementType)));
                                }
                                amount = (float) (amount * (1 - ownContainer.getResistanceValue(elementType)));
                            } else {
                                if (outcome instanceof ReactionOutcome.Additive additive) {
                                    amount = (float) (amount + additive.bonusDamage());
                                }
                                // NONE / REACTION_OCCURRED：有元素但非增幅（含 cannotApply 二级伤害）
                                if (attackerContainer != null) {
                                    amount = (float) (amount * (1 + attackerContainer.getBonusValue(elementType)));
                                }
                                amount = (float) (amount * (1 - ownContainer.getResistanceValue(elementType)));
                            }
                        } else {
                            // 这轮攻击不附带元素附着 → 物理增伤 × 物理减抗
                            LivingEntity attacker = source.getAttacker() instanceof LivingEntity livingAttacker ? livingAttacker : null;
                            ElementContainer attackerContainer = null;
                            if (attacker instanceof LivingEntityHolder attackerHolder) {
                                attackerContainer = attackerHolder.getElementContainer$EW();
                            }
                            if (attackerContainer != null) {
                                amount = (float) (amount
                                        * (1 + attackerContainer.getBonusValue(Physics.class))
                                        * (1 - ownContainer.getResistanceValue(Physics.class)));
                            } else {
                                amount = (float) (amount * (1 - ownContainer.getResistanceValue(Physics.class)));
                            }
                        }
                    }

                    /*
                    下面处理护盾对于伤害的影响
                        对于 damageHolder 的非空检查：source != null
                     */
                    Element element = damageHolder.getElement$EW();
                    // 下面对于 element 进行非空检查，若空则直接返回 null，非空则返回其类
                    if (element == null) {
                        amount = ownContainer.applyShield(null, amount);
                    } else {
                        amount = ownContainer.applyShield(element.getClass(), amount);
                    }
                }
            }
        }
        if (amount > 0) {
            Element damageElement = source instanceof DamageSourceHolder damageHolder
                    ? damageHolder.getElement$EW()
                    : null;
            FloatingTextService.spawnDamageNumber(thisLivingEntity, amount, damageElement);
        }
        args.set(1, amount);
    }
}
