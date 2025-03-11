package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Dendro;
import com.elementworld.elements.Element;
import com.elementworld.elements.Pyro;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class Combustion extends Reaction{
    private final Pyro pyro;
    private final Dendro dendro;
    private FireElement fireElement;
    public boolean die = false;

    private final float damageValue;
    private int combustionCD = 5;

    public Combustion(LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement) {
        super(owner, damageSource, firseElement, secondELement);
        if(firseElement instanceof Pyro){
            pyro = (Pyro) firseElement;
            dendro = (Dendro) secondELement;
        }
        else{
            pyro = (Pyro) secondELement;
            dendro = (Dendro) firseElement;
        }

        ElementContainer container;
        if(damageSource != null && damageSource.getAttacker() != null){
            container = ((LivingEntityHolder)damageSource.getAttacker()).getElementContainer$EW();
            if(container != null){
                damageValue = (float) (1*(1+(container.getMasteryBonus())));
            }
            else{
                damageValue = 1;
            }
        }
        else{
            damageValue = 1;
        }

    }

    public void tick(){
        //后加载燃元素实例
        if(fireElement == null){
            fireElement = new FireElement(2, owner, attacker, owner);
        }

        //如果燃元素还没有die，执行tick（）方法
        if(!fireElement.isDie){
            fireElement.tick();
        }


        if(combustionCD>0){//燃烧还在冷却
            combustionCD--;
        }
        else if(combustionCD == 0){//燃烧冷却完毕
            combustionCD=5;//重设cd
            if(!fireElement.isDie){
                //下面设立一个
                DamageSource damageSource = DamageSourceHolder.createDamageSource(owner.getWorld(),owner,attacker);
                ((DamageSourceHolder)damageSource).getEWDamageSource$EW().setElement(pyro);//此次伤害是火元素伤害
                ((DamageSourceHolder)damageSource).getEWDamageSource$EW().setCannotApply();
                owner.damage(damageSource,damageValue);//对拥有者造成伤害
            }
        }
        else{//处理意外情况
            combustionCD = 5;
        }
    }

    private static class FireElement extends Element{
        public boolean isDie = false;

        public FireElement(double gauge, LivingEntity owner, LivingEntity attacker, Entity source) {
            super(gauge, owner, attacker, source);
            decaySpeed = 0;
        }

        public void tick(){
            super.tick();
            if(!((LivingEntityHolder)owner).getElementContainer$EW().has(Dendro.class)){
                gauge = -1;
            }

            if(gauge<=0){
                isDie = true;
            }
        }
    }
}
