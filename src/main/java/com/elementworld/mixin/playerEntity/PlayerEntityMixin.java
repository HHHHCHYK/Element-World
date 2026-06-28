package com.elementworld.mixin.playerEntity;

import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.registers.ModEnchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @ModifyArgs(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private void attachElementalInfusionToAttack(Args args) {
        DamageSource source = args.get(0);
        if (!(source instanceof DamageSourceHolder damageHolder)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) (Object) this;
        Element element = ModEnchantments.getInfusionElement(player.getMainHandStack());
        if (element != null) {
            damageHolder.setDamageElement$EW(element);
        }
    }
}
