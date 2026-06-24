package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Anemo;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class Swirl extends Reaction{
    public Swirl(LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(owner, damageSource, firstElement, secondElement);
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
        Anemo anemo =(Anemo) secondElement;
        Element beSwirlElement = firstElement;
        if(beSwirlElement.getGauge()<=0){
            return;
        }
        if(anemo.getGauge() < (beSwirlElement.getGauge()*2)){//扩散原理1
            double newGauge;
            if(anemo.getGauge() < 1.5){
                newGauge = 2.2;
            }
            else{
                newGauge = 3.4;
            }
            for(LivingEntity livingEntity : livingEntities){
                DamageSource damageSource1 = DamageSourceHolder.createDamageSource(owner.getWorld(),owner,owner);
                ((DamageSourceHolder) damageSource1).getEWDamageSource$EW().setElement(Element.create(beSwirlElement.getClass(), newGauge));
                livingEntity.damage(damageSource1,3);
            }
        }else{//扩散原理2
            double newGauge = beSwirlElement.getGauge()*1.25 + 0.95;
            for(LivingEntity livingEntity : livingEntities){
                DamageSource damageSource1 = DamageSourceHolder.createDamageSource(owner.getWorld(),owner,owner);
                ((DamageSourceHolder) damageSource1).getEWDamageSource$EW().setElement(Element.create(beSwirlElement.getClass(), newGauge));
                livingEntity.damage(damageSource1,3);
            }
        }
    }
}
