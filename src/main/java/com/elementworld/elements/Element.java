package com.elementworld.elements;


import net.minecraft.entity.LivingEntity;

public class Element {
    public static enum Elements{
        Anemo,Cryo,Dendro,Electro,Frozen,Geo,Hydro,Pyro
    }

    private LivingEntity source;
    private LivingEntity owner;

    protected double gauge;
    protected double decaySpeed;

    public Element(double gauge){
        this.gauge = gauge;
        decaySpeed = this.gauge / (7+2.5* this.gauge) / 20;
    }

    /*
    这里实现元素衰减或者其他每个游戏刻都会执行的内容
     */
    public void tick(){
        gauge -= decaySpeed;
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

    public void setSource(LivingEntity source){
        this.source = source;
    }

    public void setOwner(LivingEntity owner){
        this.owner = owner;
    }

    public LivingEntity getOwner(){
        return owner;
    }

    public LivingEntity getSource(){
        return source;
    }
}

