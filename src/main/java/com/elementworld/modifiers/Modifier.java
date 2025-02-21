package com.elementworld.modifiers;

import java.util.UUID;

public class Modifier {
    //名称
    private final String name;
    //修改值
    private final double value;
    //修改方法
    private final Modifiers.modifierMethod method;
    //UUID
    UUID modifierUUID;

    //构造方法
    public Modifier(String name, double value, Modifiers.modifierMethod method){
        this.name = name;
        this.method = method;
        this.value = value;
        modifierUUID = UUID.nameUUIDFromBytes(name.getBytes());
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
}
