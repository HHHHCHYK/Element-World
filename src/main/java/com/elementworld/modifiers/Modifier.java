package com.elementworld.modifiers;

import java.util.UUID;

public class Modifier {
    public enum modifierMethod{
        ADD,MULTI
    }

    //名称
    private final String name;
    //修改值
    private final double value;
    //持续时间
    private int duration;
    //修改方法
    private final modifierMethod method;
    //UUID
    UUID modifierUUID;

    private boolean isDie = false;

    //构造方法
    public Modifier(String name, double value,int duration, modifierMethod method){
        this.name = name;
        this.method = method;
        this.value = value;
        this.duration = duration;
        modifierUUID = UUID.nameUUIDFromBytes(name.getBytes());
    }

    public void tick(){
        if(duration>0){
            duration--;
        }
        else {
            isDie = true;
        }
    }

    //应用修改器
    public float apply(float base_amount){
        switch (method){
            case ADD -> {return base_amount + (float) value;}
            case MULTI -> {return base_amount * (float) value;}
            default -> {return 1;}
        }
    }

    //getter & setter
    public String getName() {
        return name;
    }

    public UUID getModifierUUID() {
        return modifierUUID;
    }

    public boolean isDie(){
        return isDie;
    }
}
