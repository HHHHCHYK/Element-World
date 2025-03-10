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

public interface DamageSourceHolder {
    static DamageSource createDamageSource(World world, LivingEntity source, LivingEntity attacker) {
        Registry<DamageType> damageTypeRegistry = source.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        DamageType type = damageTypeRegistry.get(ElementWorld.ELEMENT_DAMAGE);

        return new DamageSource(damageTypeRegistry.getEntry(type), source,attacker);
    }

    Element getElement$EW();
    void setDamageElement$EW(Element element);
    void setDamageType$EW(EWDamageSource.DAMAGE_TYPE damageType);
    EWDamageSource getEWDamageSource$EW();
}
