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
    private ElementContainer elementContainer;


    @Inject(method = "tick", at = @At("HEAD"))
    public void livingEntityTickMixin(CallbackInfo info) {
        //鎳掑姞杞?
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        this.elementContainer.tick();
    }

    @Unique
    @Override
    public ElementContainer getElementContainer$EW() {
        //鎳掑姞杞?
        if (elementContainer == null) {
            elementContainer = new ElementContainer((LivingEntity) (Object) this);
        }
        return elementContainer;
    }


    //鏇存敼浼ゅ鍒ゅ畾
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
        LivingEntity thisLivingEntity = (LivingEntity) (Object) this;//鑾峰彇褰撳墠鐢熺墿鐨勫疄渚?


        if (amount <= 0) {
            return;
        }

        if (source != null) {
            /*
             * 1.鑾峰彇鏀诲嚮鑰呯殑鍏冪礌瀹瑰櫒
             */

            if (thisLivingEntity == null) {//绌哄€兼鏌?
                return;
            }
            ElementContainer ownContainer = ((LivingEntityHolder) this).getElementContainer$EW();//鑾峰彇鑷韩鍏冪礌瀹瑰櫒

            if (!thisLivingEntity.isInvulnerableTo(source)) {//鍒ゆ柇鐢熺墿鏄惁瀵逛簬鏉ユ簮鏃犳晫
                if (source instanceof DamageSourceHolder damageHolder) {
                    if (damageHolder.getEWDamageSource$EW() == null) {//绌哄€兼鏌?
                        return;
                    }
                    if (!ownContainer.isImmune(damageHolder.getEWDamageSource$EW().getEP())) {//鍒ゆ柇鐢熺墿鏄惁鏈夊鏌愪釜鍏冪礌鐨勫厤鐤?
                        Element element = damageHolder.getElement$EW();
                        if (element != null) {//濡傛灉杩欐鏀诲嚮甯︽湁鍏冪礌闄勭潃

                            Reaction reaction;
                            if (!damageHolder.getEWDamageSource$EW().isCannotApply()) {//鍒ゆ柇杩欐鍙嶅簲鏄惁閫犳垚鍏冪礌闄勭潃
                                /*
                                杩欎竴姝ヤ负鍏冪礌鍙嶅簲瀹瑰櫒娣诲姞闄勭潃
                                 */
                                reaction = ownContainer.applyElement(element, source);//娣诲姞鍏冪礌锛岃幏寰楀弽搴斿疄渚?


                                if (source.getAttacker() != null) {//瀛樺湪鏀诲嚮鑰?
                                    ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                    if (attackerContainer != null) {
                                        if (reaction != null) {//濡傛灉浜х敓浜嗗厓绱犲弽搴?


                                            if (reaction instanceof AmpReaction ampReaction) {//濡傛灉鍙嶅簲绫诲瀷涓哄骞呭弽搴?
                                                amount = (float) (amount
                                                        * (1 + (ampReaction.getReactionBaseMul()))//鍏冪礌鍙嶅簲澧炰激
                                                        * AmpReaction.getMasteryAmp(attackerContainer.getMastery())//绮鹃€氬浼?
                                                        * (1 + attackerContainer.getBonusValue(Element.toEpClass(element.getClass())))//澧炰激涔樺尯
                                                        * (1 - ownContainer.getResistanceValue(Element.toEpClass(element.getClass())))//鍑忔姉涔樺尯
                                                );
                                            } else {
                                                reaction.apply();//杩愯涓€娆″厓绱犲弽搴斿唴瀹?
                                            }
                                        } else {//濡傛灉娌℃湁浜х敓鍏冪礌鍙嶅簲
                                            Class<? extends EP> elementType = element.getClass();
                                            amount = (float) (amount
                                                    * (1 + attackerContainer.getBonusValue(elementType))
                                                    * (1 - ownContainer.getResistanceValue(elementType))
                                            );
                                        }
                                    }
                                } else {//濡傛灉鏀诲嚮鑰呬负绌?
                                    if (reaction != null) {//濡傛灉浜х敓浜嗗厓绱犲弽搴?


                                        if (reaction instanceof AmpReaction ampReaction) {//濡傛灉鍙嶅簲绫诲瀷涓哄骞呭弽搴?
                                            amount = (float) (amount
                                                    * (1 + (ampReaction.getReactionBaseMul()))//鍏冪礌鍙嶅簲澧炰激
                                                    * (1 - ownContainer.getResistanceValue(Element.toEpClass(element.getClass())))//鍑忔姉涔樺尯
                                            );
                                        } else {
                                            reaction.apply();//杩愯涓€娆″厓绱犲弽搴斿唴瀹?
                                        }
                                    } else {//濡傛灉娌℃湁浜х敓鍏冪礌鍙嶅簲
                                        Class<? extends EP> elementType = element.getClass();
                                        amount = (float) (amount
                                                * (1 - ownContainer.getResistanceValue(elementType))
                                        );
                                    }
                                }
                            } else {//濡傛灉涓嶉€犳垚鍏冪礌闄勭潃
                                amount = (float) (amount
                                        * (1 - ownContainer.getResistanceValue(Element.toEpClass(element.getClass())))
                                );
                                /*
                                濡傛灉瀛樺湪鏀诲嚮鑰咃紝鍒欒绠楀浼や箻鍖?
                                 */
                                if (source.getAttacker() != null) {//濡傛灉瀛樺湪鏀诲嚮鑰?
                                    ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                    if (attackerContainer != null) {
                                        amount = (float) (amount
                                                * (1 + attackerContainer.getBonusValue(Element.toEpClass(element.getClass())))
                                        );
                                    }
                                }
                            }

                        } else {//杩欐鏀诲嚮涓嶉檮甯﹀厓绱犻檮鐫€
                            if (source.getAttacker() != null) {//瀛樺湪鏀诲嚮鑰?

                                ElementContainer attackerContainer = ((LivingEntityHolder) source.getAttacker()).getElementContainer$EW();
                                if (attackerContainer != null) {//绌哄€兼鏌?
                                    amount = (float) (amount
                                            * (1 + attackerContainer.getBonusValue(Physics.class))
                                            * (1 - ownContainer.getResistanceValue(Physics.class))
                                    );
                                } else {//涓嶅瓨鍦ㄦ敾鍑昏€?
                                    amount = (float) (amount
                                            * (1 - ownContainer.getResistanceValue(Physics.class))
                                    );
                                }

                            }
                        }
                    }

                    /*
                    涓嬮潰澶勭悊鎶ょ浘瀵逛簬浼ゅ鐨勫奖鍝?
                        瀵逛簬damageHolder鐨勯潪绌烘鏌ワ細source 锛? null
                     */
                    Element element = damageHolder.getElement$EW();
                    //涓嬮潰瀵逛簬element杩涜闈炵┖妫€鏌ワ紝濡傝嫢绌哄垯鐩存帴杩斿洖null锛岄潪绌哄垯杩斿洖鍏剁被
                    if (element == null) {
                        amount = ownContainer.applyShield(null, amount);
                    } else {
                        amount = ownContainer.applyShield(element.getClass(), amount);
                    }
                }
            }
        }
        args.set(1, amount);
    }
}


