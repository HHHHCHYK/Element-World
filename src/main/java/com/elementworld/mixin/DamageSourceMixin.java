package com.elementworld.mixin;


import com.elementworld.elementComponents.EWDamageSource;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceHolder {

    @Unique
    public EWDamageSource ewDamageSource = new EWDamageSource((DamageSource) (Object) this);

    @Unique @Nullable
    public Element getElement$EW() {
        if (ewDamageSource != null) {
            return ewDamageSource.getDamageElement();
        }else{
            return null;
        }
    }

    @Unique
    public void setDamageElement$EW(Element element){
        if(ewDamageSource != null){
            if(!ewDamageSource.setElement(element)){
                System.out.println("Set element failed");
            }
        }

    }

    @Unique @Override
    public void setDamageType$EW(EWDamageSource.DAMAGE_TYPE damageType) {
        if (ewDamageSource != null) {
            ewDamageSource.setDamageType(damageType);
        }
    }


    @Unique @Override
    public EWDamageSource getEWDamageSource$EW() {
        if(ewDamageSource == null){
            ewDamageSource = new EWDamageSource((DamageSource) (Object) this);
        }
        return ewDamageSource;
    }


}
