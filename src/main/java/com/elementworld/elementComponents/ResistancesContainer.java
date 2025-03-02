package com.elementworld.elementComponents;

import com.elementworld.elements.*;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ResistancesContainer {
    /*
        抗性实例
        成员：
            抗性元素类别
            抗性数值
            实例名称
        作用：
            用于记录实体抗性的实例
         */
        public record ResistanceInstance(Class<? extends EP> elementType, double resistanceValue, String name) {
            public ResistanceInstance(@Nullable Class<? extends EP> elementType, double resistanceValue, String name) {
                this.elementType = elementType;
                this.resistanceValue = resistanceValue;
                this.name = name;
            }
        }

    /*
    不同元素的抗性实例集
     */
    private static class ResistanceSet {
        public HashMap<String, ResistanceInstance> resistanceInstances = new HashMap<>();
        public final Class<? extends EP> elementType;

        public ResistanceSet(Class<? extends EP> elementType) {
            this.elementType = elementType;
        }

        public void addResistance(double value, String name) {
            if (resistanceInstances.containsKey(name)) {
                System.out.println("抗性实例 " + name + " 已存在");
                return;
            }
            resistanceInstances.put(name, new ResistanceInstance(elementType, value, name));
        }

        public boolean removeResistance(String name) {
            if (resistanceInstances.containsKey(name)) {
                resistanceInstances.remove(name);
                return true;
            }
            return false;
        }

        public double apply() {
            double totalResistance = 0.0;
            for (ResistanceInstance instance : resistanceInstances.values()) {
                totalResistance += instance.resistanceValue();
            }
            return totalResistance;
        }
    }

    /*
    不同元素的抗性集合
     */
    private final ResistanceSet hydroResistSet;
    private final ResistanceSet pyroResistSet;
    private final ResistanceSet anemoResistSet;
    private final ResistanceSet cryoResistSet;
    private final ResistanceSet dendroResistSet;
    private final ResistanceSet electroResistSet;
    private final ResistanceSet geoResistSet;
    private final ResistanceSet physicsResistSet;

    /*
    容器属性
     */
    private final LivingEntity owner;

    public ResistancesContainer(LivingEntity owner) {
        this.owner = owner;
        // 初始化所有抗性集合
        hydroResistSet = new ResistanceSet(Hydro.class);
        pyroResistSet = new ResistanceSet(Pyro.class);
        anemoResistSet = new ResistanceSet(Anemo.class);
        cryoResistSet = new ResistanceSet(Cryo.class);
        dendroResistSet = new ResistanceSet(Dendro.class);
        electroResistSet = new ResistanceSet(Electro.class);
        geoResistSet = new ResistanceSet(Geo.class);
        physicsResistSet = new ResistanceSet(null); // 物理抗性
    }

    // 添加抗性
    public void addResistance(Class<? extends EP> elementType, double value, String name) {
        ResistanceSet set = getResistanceSet(elementType);
        if (set != null) {
            set.addResistance(value, name);
        }
    }

    // 移除抗性
    public boolean removeResistance(Class<? extends EP> elementType, String name) {
        ResistanceSet set = getResistanceSet(elementType);
        return set != null && set.removeResistance(name);
    }

    // 获取总抗性值
    public double getResistanceValue(@Nullable Class<? extends EP> elementType) {
        ResistanceSet set = getResistanceSet(elementType);
        return set != null ? set.apply() : 0.0;
    }

    // 根据元素类型获取对应的抗性集合
    private ResistanceSet getResistanceSet(Class<? extends EP> elementType) {
        if (elementType == Hydro.class) {
            return hydroResistSet;
        } else if (elementType == Pyro.class) {
            return pyroResistSet;
        } else if (elementType == Anemo.class) {
            return anemoResistSet;
        } else if (elementType == Cryo.class) {
            return cryoResistSet;
        } else if (elementType == Dendro.class) {
            return dendroResistSet;
        } else if (elementType == Electro.class) {
            return electroResistSet;
        } else if (elementType == Geo.class) {
            return geoResistSet;
        } else if (elementType == Physics.class) {
            return physicsResistSet;
        } else {
            return null; // 默认处理物理抗性
        }
    }

    public LivingEntity getOwner(){
        return owner;
    }
}