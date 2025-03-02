package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class Reaction {
    protected final LivingEntity owner;
    protected final LivingEntity attacker;
    public boolean die = false;

    protected final Element secondElement;

    public final float damageValue;

    public Reaction(LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement){

        //标定拥有者，施加者，还有参与反应的两个元素实例
        this.owner = owner;
        if(damageSource.getAttacker() instanceof LivingEntity){
            this.attacker =(LivingEntity) damageSource.getAttacker();
        }
        else {
            attacker = null;
        }

        //初始化反应伤害
        damageValue = (float) (0.6*((1+secondELement.getOwnerContainer().getMasteryBonus())));

        this.secondElement = secondELement;
    }
}
