package com.elementworld.elements;


import net.minecraft.entity.LivingEntity;

public class Element {

    public static enum ElementType {
        ANEMO,CRYO,DENDRO,ELECTRO,FROZEN,GEO,HYDRO,PYRO
    }

    //拥有者和施加者
    private LivingEntity source;
    private LivingEntity owner;

    //元素量和衰减速度
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
    下面是一些功能函数
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
            default -> {
                return null;
            }
        }
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

