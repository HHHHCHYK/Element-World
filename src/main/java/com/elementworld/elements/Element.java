package com.elementworld.elements;


import com.elementworld.ElementContainer;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Element extends EP {

    public enum ElementType {
        ANEMO,CRYO,DENDRO,ELECTRO,FROZEN,GEO,HYDRO,PYRO,Quicken,Physics
    }

    public static Class<? extends EP> enumToClass(ElementType elementType){
        switch (elementType){
            case ELECTRO -> {
                return Electro.class;
            }
            case PYRO -> {
                return Pyro.class;
            }
            case HYDRO -> {
                return Hydro.class;
            }
            case CRYO -> {
                return Cryo.class;
            }
            case ANEMO -> {
                return Anemo.class;
            }
            case GEO -> {
                return Geo.class;
            }
            case DENDRO -> {
                return Dendro.class;
            }
            case FROZEN -> {
                return Frozen.class;
            }
            case Quicken -> {
                return Catalyze.class;
            }
            case Physics -> {
                return Physics.class;
            }
            default -> {
                return null;
            }
        }
    }

    //拥有者和施加者
    protected LivingEntity attacker;
    protected LivingEntity owner;
    protected Entity source;
    protected ElementContainer ownerContainer;

    //元素量和衰减速度
    protected double gauge;
    protected double decaySpeed;


    public Element(double gauge){
        this.gauge = gauge;
        decaySpeed = this.gauge / (7+2.5* this.gauge) / 20;
    }

    public Element(double gauge,LivingEntity owner,LivingEntity attacker,Entity source){
        this.gauge = gauge;
        decaySpeed = this.gauge / (7+2.5* this.gauge) / 20;
        this.attacker = attacker;
        this.owner = owner;
        this.source = source;

        if (owner != null) {
            ownerContainer = ((LivingEntityHolder)owner).getElementContainer$EW();
        }
    }

    /*
    这里实现元素衰减或者其他每个游戏刻都会执行的内容
     */
    public void tick(){
        gauge -= decaySpeed;
    }

    /*
    下面是一些功能函数(静态）
     */

    //创建一个元素实例
    public static Element create(ElementType elementType, double gauge){
        switch (elementType){
            case GEO -> {
                return new Geo(gauge);
            }
            case CRYO -> {
                return new Cryo(gauge);
            }
            case PYRO -> {
                return new Pyro(gauge);
            }
            case ANEMO -> {
                return new Anemo(gauge);
            }
            case HYDRO -> {
                return new Hydro(gauge);
            }
            case DENDRO -> {
                return new Dendro(gauge);
            }
            case FROZEN -> {
                return new Frozen(gauge);
            }
            case ELECTRO -> {
                return new Electro(gauge);
            }
            case Quicken -> {
                return new Catalyze(gauge);
            }
            default -> {
                return null;
            }
        }
    }

    public static Element create(Class<?extends EP> elementType, double gauge){
        if (elementType.equals(Geo.class)) {
            return new Geo(gauge);
        } else if (elementType.equals(Cryo.class)) {
            return new Cryo(gauge);
        } else if (elementType.equals(Pyro.class)) {
            return new Pyro(gauge);
        } else if (elementType.equals(Anemo.class)) {
            return new Anemo(gauge);
        } else if (elementType.equals(Hydro.class)) {
            return new Hydro(gauge);
        } else if (elementType.equals(Dendro.class)) {
            return new Dendro(gauge);
        } else if (elementType.equals(Frozen.class)) {
            return new Frozen(gauge);
        } else if (elementType.equals(Electro.class)) {
            return new Electro(gauge);
        } else if (elementType.equals(Catalyze.class)) {
            return new Catalyze(gauge);
        }
        return null;
    }

    //将元素类转为Element&Physics类
    public static Class<? extends EP> ToEP(@Nullable Class<?extends Element> element){
        return Objects.requireNonNullElse(element, Physics.class);
    }


    /*
    以下是getter&setter
     */
    public double getGauge(){
        return gauge;
    }

    public void subGauge(double gauge){
        this.gauge -= gauge;
    }

    public void addGauge(double gauge){
        this.gauge +=gauge;
    }

    public void setGauge(double gauge){
        this.gauge = gauge;
    }

    public void setAttacker(LivingEntity attacker){
        this.attacker = attacker;
    }

    public void setOwner(LivingEntity owner){
        this.owner = owner;
    }

    public LivingEntity getOwner(){
        return owner;
    }

    public LivingEntity getAttacker(){
        return attacker;
    }

    public ElementContainer getOwnerContainer(){
        return ownerContainer;
    }

    public Entity getSource(){return source;}
}

