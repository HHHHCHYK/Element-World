package com.elementworld.elements;

import com.elementworld.ElementContainer;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Element extends EP {
    public enum ElementType {
        ANEMO,
        CRYO,
        DENDRO,
        ELECTRO,
        FROZEN,
        GEO,
        HYDRO,
        PYRO,
        QUICKEN,
        PHYSICS
    }

    protected LivingEntity attacker;
    protected LivingEntity owner;
    protected Entity source;
    protected ElementContainer ownerContainer;
    protected double gauge;
    protected double decaySpeed;

    public Element(double gauge) {
        this.gauge = gauge;
        decaySpeed = this.gauge / (7 + 2.5 * this.gauge) / 20;
    }

    public Element(double gauge, LivingEntity owner, LivingEntity attacker, Entity source) {
        this(gauge);
        this.attacker = attacker;
        this.owner = owner;
        this.source = source;

        if (owner instanceof LivingEntityHolder holder) {
            ownerContainer = holder.getElementContainer$EW();
        }
    }

    public static Class<? extends EP> enumToClass(ElementType elementType) {
        return switch (elementType) {
            case ELECTRO -> Electro.class;
            case PYRO -> Pyro.class;
            case HYDRO -> Hydro.class;
            case CRYO -> Cryo.class;
            case ANEMO -> Anemo.class;
            case GEO -> Geo.class;
            case DENDRO -> Dendro.class;
            case FROZEN -> Frozen.class;
            case QUICKEN -> Catalyze.class;
            case PHYSICS -> Physics.class;
        };
    }

    public static Element create(ElementType elementType, double gauge) {
        return switch (elementType) {
            case GEO -> new Geo(gauge);
            case CRYO -> new Cryo(gauge);
            case PYRO -> new Pyro(gauge);
            case ANEMO -> new Anemo(gauge);
            case HYDRO -> new Hydro(gauge);
            case DENDRO -> new Dendro(gauge);
            case FROZEN -> new Frozen(gauge);
            case ELECTRO -> new Electro(gauge);
            case QUICKEN -> new Catalyze(gauge);
            case PHYSICS -> null;
        };
    }

    public static Element create(Class<? extends EP> elementType, double gauge) {
        if (elementType == Geo.class) {
            return new Geo(gauge);
        } else if (elementType == Cryo.class) {
            return new Cryo(gauge);
        } else if (elementType == Pyro.class) {
            return new Pyro(gauge);
        } else if (elementType == Anemo.class) {
            return new Anemo(gauge);
        } else if (elementType == Hydro.class) {
            return new Hydro(gauge);
        } else if (elementType == Dendro.class) {
            return new Dendro(gauge);
        } else if (elementType == Frozen.class) {
            return new Frozen(gauge);
        } else if (elementType == Electro.class) {
            return new Electro(gauge);
        } else if (elementType == Catalyze.class) {
            return new Catalyze(gauge);
        }
        return null;
    }

    public static Class<? extends EP> toEpClass(@Nullable Class<? extends Element> element) {
        return Objects.requireNonNullElse(element, Physics.class);
    }

    public void tick() {
        gauge -= decaySpeed;
    }

    public double getGauge() {
        return gauge;
    }

    public void subGauge(double gauge) {
        this.gauge -= gauge;
    }

    public void addGauge(double gauge) {
        this.gauge += gauge;
    }

    public void setGauge(double gauge) {
        this.gauge = gauge;
    }

    public void setAttacker(LivingEntity attacker) {
        this.attacker = attacker;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public ElementContainer getOwnerContainer() {
        return ownerContainer;
    }

    public Entity getSource() {
        return source;
    }
}
