package com.elementworld.elementComponents;

import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import com.elementworld.elements.Physics;
import com.elementworld.interfaces.DamageSourceHolder;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EWDamageSource {
    public @Nullable DamageSourceHolder getDamageSourceHolder() {
        return damageSourceHolder;
    }

    public enum DAMAGE_TYPE{
        AmpReaction,TraReaction,Dot,Physics
    }

    @NotNull
    private final DamageSource originDamageSource;

    @Nullable
    private DamageSourceHolder damageSourceHolder;

    private Element damageElement;
    private DAMAGE_TYPE damageType = DAMAGE_TYPE.AmpReaction;
    private boolean cannotApply = true;

    public EWDamageSource(@NotNull DamageSource damageSource){
        originDamageSource = damageSource;
        if(damageSource instanceof DamageSourceHolder holder){
            damageSourceHolder = holder;
        }
    }

    public boolean setElement(Element element){
        try {
            if(element == null){
                damageElement = null;
                return true;
            }else {
                damageElement = element;
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public Element getDamageElement(){
        return damageElement;
    }

    @NotNull
    public DamageSource getDamageSource(){
        return originDamageSource;
    }

    public DAMAGE_TYPE getDamageType(){
        return damageType;
    }

    public void setDamageType(DAMAGE_TYPE damageType){
        this.damageType = damageType;
    }

    public Class<?extends EP> getEP(){
        return Objects.requireNonNullElse(damageElement.getClass(), Physics.class);
    }

    public boolean isCannotApply(){
        return cannotApply;
    }

    public void setCannotApply(){
        cannotApply = false;
    }

    public void resetCannotApply(){
        cannotApply = true;
    }
}
