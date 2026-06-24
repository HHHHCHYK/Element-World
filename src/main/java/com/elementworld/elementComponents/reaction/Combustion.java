package com.elementworld.elementComponents.reaction;

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

    private int combustionCD = 5;

    public Combustion(LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(owner, damageSource, firstElement, secondElement);
        if(firstElement instanceof Pyro){
            pyro = (Pyro) firstElement;
            dendro = (Dendro) secondElement;
        }
        else{
            pyro = (Pyro) secondElement;
            dendro = (Dendro) firstElement;
        }
        //反应伤害已在父类构造函数中依据 attacker 容器的精通加成计算完毕,这里无需重复计算
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
