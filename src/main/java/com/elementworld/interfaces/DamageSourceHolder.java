package com.elementworld.interfaces;

import com.elementworld.ElementWorld;
import com.elementworld.elementComponents.EWDamageSource;
import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface DamageSourceHolder {
    static DamageSource createDamageSource(World world, LivingEntity source, @Nullable LivingEntity attacker) {
        Registry<DamageType> damageTypeRegistry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        DamageType type = damageTypeRegistry.get(ElementWorld.ELEMENT_DAMAGE);
        return new DamageSource(damageTypeRegistry.getEntry(type), source, attacker);
    }

    @Nullable
    Element getElement$EW();

    void setDamageElement$EW(@Nullable Element element);

    void setDamageKind$EW(EWDamageSource.DamageKind damageKind);

    EWDamageSource getEWDamageSource$EW();
}
