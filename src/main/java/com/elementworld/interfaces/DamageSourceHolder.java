package com.elementworld.interfaces;

import com.elementworld.ElementWorld;
import com.elementworld.elements.Element;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface DamageSourceHolder {

    Element elementWorld$getElement();
    DamageSource elementWorld$setDamageElementType(Element element);
    boolean elementWorld$isNormalReaction();
    DamageSource elementWorld$setNormal();

    public static DamageSource createDamageSource(World world, Entity source, @Nullable LivingEntity attacker){
        Registry<DamageType> damageTypeRegistry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        DamageType type = damageTypeRegistry.get(ElementWorld.ELEMENT_DAMAGE);
        return new DamageSource(damageTypeRegistry.getEntry(type),source,attacker);
    }

    public static DamageSourceHolder setNormal(DamageSource damageSource){
        return (DamageSourceHolder) ((DamageSourceHolder)damageSource).elementWorld$setNormal();
    }

    public static DamageSourceHolder setDamageElement(DamageSource damageSource,Element element){
        return (DamageSourceHolder)
                ((DamageSourceHolder)damageSource).elementWorld$setDamageElementType(element);
    }

}
