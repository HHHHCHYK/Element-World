package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class Swirl extends Reaction{
    public Swirl(LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement) {
        super(owner, damageSource, firseElement, secondELement);
    }

    public void apply(){
        World world = owner.getWorld();
        double x = owner.getX();double y = owner.getY();double z = owner.getZ();
        double range = 5;
        List<LivingEntity> livingEntities = world.getEntitiesByClass(
                LivingEntity.class,
                new Box(x-range,y-range,z-range,x+range,y+range,z+range),
                livingEntity -> livingEntity instanceof LivingEntityHolder
        );
    }
}
