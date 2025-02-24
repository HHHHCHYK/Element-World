package com.elementworld.modifiers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Modifiers {

    //用于标识这个列表的操作目标
    public enum modifierType{
        MAX_HEALTH,
        BASE_DAMAGE,
        MASTERY,
        RESISTANCE,
        BONUS
    }


    //构造方法
    public Modifiers(modifierType target){
        this.type = target;
    }

    private ArrayList<Modifier> deadModifiers;

    public void tick(){
        for(Modifier modifier : modifiers){
            if(modifier.isDie()){
                if(deadModifiers == null){deadModifiers = new ArrayList<>();}
                deadModifiers.add(modifier);
            }
            modifier.tick();
        }

        for(Modifier modifier : deadModifiers){
            modifiers.remove(modifier);
        }
        deadModifiers.clear();
    }

    private final modifierType type;

    ArrayList<Modifier> modifiers;
    HashMap<UUID,Modifier> modifierUUIDHashMap;//提供通过uuid查找修改器的方法


    //增减修改器
    public void addModifier(String name, double value,int duration, Modifier.modifierMethod method){
        if(modifiers == null){
            modifiers = new ArrayList<>();
        }
        Modifier modifier = new Modifier(name,value,duration,method);
        modifierUUIDHashMap.put(UUID.nameUUIDFromBytes(name.getBytes()),modifier);
        modifiers.add(modifier);
    }

    public void addModifier(@NotNull Modifier modifier){
        if(modifiers == null){
            modifiers = new ArrayList<>();
        }
        modifierUUIDHashMap.put(UUID.nameUUIDFromBytes(modifier.getName().getBytes()),modifier);
        modifiers.add(modifier);
    }

    public  void removeModifier(@NotNull Modifier modifier){
        modifiers.remove(modifier);
    }

    public void removeModifier(String name){
        modifiers.remove(modifierUUIDHashMap.get(UUID.nameUUIDFromBytes(name.getBytes())));
    }

    //应用修改器
    public float applyModifiers (float base_amount){
        for(Modifier modifier : modifiers){
            base_amount = modifier.apply(base_amount);
        }
        return base_amount;
    }

    //getter & setter
    public modifierType getType() {
        return type;
    }

    public ArrayList<Modifier> getModifiers(){
        return modifiers;
    }
}
