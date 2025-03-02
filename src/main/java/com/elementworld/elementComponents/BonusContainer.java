package com.elementworld.elementComponents;

import com.elementworld.elements.*;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class BonusContainer {
    /*
        （增伤实例）
        成员：
            增伤元素类别
            增伤数值
        作用：
            用于记录实体增伤的实例
         */
        public record BonusInstance(Class<? extends EP> elementType, double bonusValue, String name) {
            public BonusInstance(@Nullable Class<? extends EP> elementType, double bonusValue, String name) {
                this.elementType = elementType;
                this.bonusValue = bonusValue;
                this.name = name;
            }
        }


    /*
    不同元素的抗性实例集
     */
    private static class BonusSet{
        public HashMap<String,BonusInstance> bonusInstances = new HashMap<>();

        public final Class<? extends EP> elementType;

        public BonusSet(Class<? extends EP> elementType){
            this.elementType = elementType;
        }

        public void addBonus(double value,String name){
            if(bonusInstances.containsKey(name)){
                System.out.println("The BonusInstance already exists");
                return;
            }
            bonusInstances.put(name,new BonusInstance(elementType,value,name));
        }

        public boolean removeBonus(String name){
            if(bonusInstances.containsKey(name)){
                bonusInstances.remove(name);
                return true;
            }
            else {
                return false;
            }
        }

        public double apply(){
            double value = 0;
            for(BonusInstance bonusInstance : bonusInstances.values()){
                value += bonusInstance.bonusValue();
            }
            return value;
        }
    }

    /*
    下面列出了所以增伤集成员
     */
    private BonusSet hydroSet;
    private BonusSet pyroSet;
    private BonusSet anemoSet;
    private BonusSet cryoSet;
    private BonusSet dendroSet;
    private BonusSet electroSet;
    private BonusSet geoSet;
    private BonusSet physicsSet;

    /*
    下面是容器本身的性质
     */
    LivingEntity owner;

    public BonusContainer(LivingEntity owner){
        this.owner = owner;
    }

    public void addBonus(Class<? extends EP> elementType,double value,String name){
        BonusSet set = judgeBonusSet(elementType);
        if(set == null){
            set = new BonusSet(elementType);
        }

        set.addBonus(value, name);
    }

    public boolean removeBonus(Class<? extends EP> elementType,String name){
        BonusSet set = judgeBonusSet(elementType);
        if(set == null){
            return false;
        }
        else {
            return set.removeBonus(name);
        }
    }

    public double getBonusValue(@Nullable Class<? extends EP> elementType){
        return judgeBonusSet(elementType).apply();
    }

    private  BonusSet judgeBonusSet(Class<?extends EP> elementType){
        if (elementType == Hydro.class) {
            return hydroSet;
        } else if (elementType == Pyro.class) {
            return pyroSet;
        } else if (elementType == Anemo.class) {
            return anemoSet;
        } else if (elementType == Cryo.class) {
            return cryoSet;
        } else if (elementType == Dendro.class) {
            return dendroSet;
        } else if (elementType == Electro.class) {
            return electroSet;
        } else if (elementType == Geo.class) {
            return geoSet;
        } else if (elementType == Physics.class) {
            return physicsSet;
        } else {
            return null; // 默认处理物理抗性
        }

    }

    






}
