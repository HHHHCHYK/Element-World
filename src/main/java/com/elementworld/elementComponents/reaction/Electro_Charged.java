package com.elementworld.elementComponents.reaction;

import com.elementworld.Calculater;
import com.elementworld.elements.Electro;
import com.elementworld.elements.Element;
import com.elementworld.elements.Hydro;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Box;

import java.util.List;

public class Electro_Charged extends Reaction{

    private final Hydro hydro;
    private final Electro electro;

    private int reactionCD = 20;

    public boolean die = false;

    public Electro_Charged(LivingEntity owner,DamageSource damageSource,Element firseElement,Element secondELement){
        super(owner,damageSource,firseElement,secondELement);
        if(firseElement instanceof Hydro){
            hydro = (Hydro) firseElement;
            electro = (Electro) secondELement;
        }
        else{
            hydro = (Hydro) secondELement;
            electro = (Electro) firseElement;
        }
    }

    public void tick(){
        if(hydro.getGauge()<=0 || electro.getGauge() <= 0){
            die = true;
            System.out.println("die");
            return;
        }
        if(reactionCD > 0){
            reactionCD--;
        }
        else if(reactionCD == 0){
            hydro.subGauge(0.4);
            electro.subGauge(0.4);

            DamageSource reactionDamage = DamageSourceHolder.createDamageSource(owner.getWorld(),owner,attacker);
            owner.damage(reactionDamage,damageValue);

            double x = owner.getX();double y = owner.getY();double z = owner.getZ();double range = 5;

            List<LivingEntity> livingEntityList = owner.getWorld().getEntitiesByClass(
                    LivingEntity.class,
                    new Box(x-range,y-range,z-range,x+range,y+range,z+range),
                    livingEntity -> livingEntity instanceof LivingEntityHolder
            );
            for(LivingEntity livingEntity : livingEntityList){
                if(livingEntity instanceof LivingEntityHolder && Calculater.distance(livingEntity.getPos(),owner.getPos()) < 5){
                    livingEntity.damage(reactionDamage,damageValue);
                }
            }
            reactionCD = 20;
        }
        else{
            reactionCD = 20;
        }
    }
}
