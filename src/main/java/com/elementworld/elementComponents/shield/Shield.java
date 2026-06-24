package com.elementworld.elementComponents.shield;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Element;
import com.elementworld.elements.Geo;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Shield {

    private final LivingEntity owner;
    private double value;
    private final double maxValue;
    private final String name;
    private final UUID uuid;
    private Class<? extends Element> element = null;

    public Shield(String name,LivingEntity owner,double maxValue){
        this.owner = owner;
        this.maxValue = maxValue;
        this.value = maxValue;
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(name.getBytes());
    }

    public Shield(String name,LivingEntity owner,double maxValue,Class<? extends Element> element){
        this.owner = owner;
        this.maxValue = maxValue;
        this.value = maxValue;
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(name.getBytes());
        this.element = element;
    }

    /*
    护盾应用：
        输入一个伤害值，通过this的元素类型和值计算出被抵消之后的伤害，然后返回抵消后的伤害值
     */
    public double apply(double damage,@Nullable Class<? extends Element> element){
        if(owner instanceof LivingEntityHolder holder){//如果正常加载Mixin
            ElementContainer container = holder.getElementContainer$EW();//获取元素容器
            if(this.element == Geo.class){//岩元素护盾全元素强效150%
                double min  = Math.min(damage, value*1.5*(container.getShieldStrength()));
                subValue(min/1.5*(container.getShieldStrength()));
                return damage - min;
            }
            else if(this.element == element){//同元素护盾对应元素强效250
                double min = Math.min(damage, value*2.5*(container.getShieldStrength()));
                subValue(min/2.5*(container.getShieldStrength()));
                return damage - min;
            }
            else{//其余强效100%
                double min = Math.min(damage, value*(container.getShieldStrength()));
                subValue(min/(container.getShieldStrength()));
                return damage - min;
            }
        }
        else{
            return damage;
        }


    }

    public void subValue(double value){
        this.value -= value;
    }

    public boolean isDie(){
        return value <= 0;
    }

    public void recover(double value){
        this.value += value;
        if(this.value > maxValue){
            this.value = maxValue;
        }
    }

    public double getValue(){
        return value;
    }

    public UUID getUuid(){
        return uuid;
    }

    public String getName(){
        return name;
    }

    public Class<? extends Element> getElement(){
        return element;
    }
}
