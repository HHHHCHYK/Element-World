package com.elementworld.elements;


import net.minecraft.entity.LivingEntity;

public class Element {
    public static enum Elements{
        Anemo,Cryo,Dendro,Electro,Frozen,Geo,Hydro,Pyro
    }

    protected double gauge;
    protected double decaySpeed;

    public Element(double gauge){
        this.gauge = gauge;
        decaySpeed = this.gauge / (7+2.5* this.gauge) / 20;
    }

    /*
    这里实现元素衰减或者其他每个游戏刻都会执行的内容
     */
    public void tick(LivingEntity entity){
        gauge -= decaySpeed;
    }

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



}

