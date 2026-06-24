package com.elementworld.elementComponents.reaction;

import com.elementworld.Calculator;
import com.elementworld.ElementContainer;
import com.elementworld.ElementWorld;
import com.elementworld.elementComponents.modifiers.Modifier;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elements.Electro;
import com.elementworld.elements.Element;
import com.elementworld.elements.Pyro;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class TFReaction extends Reaction{

    public TFReaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(reactionType, owner, damageSource, firstElement, secondElement);
    }

    /*
    reactionType有可能是OVERLOAD,SUPERCONDUCT,SHATTER；
     */
    public void apply(){
        //获取攻击者元素反应容器
        ElementContainer attackerElementContainer = null;
        if(attacker instanceof LivingEntityHolder livingEntityHolder){
            attackerElementContainer = livingEntityHolder.getElementContainer$EW();
        }
        ElementContainer ownerElementContainer = ((LivingEntityHolder)owner).getElementContainer$EW();

        switch (reactionType){
            case OVERLOAD -> {//如果是超载反应
                Pyro pyro;
                Electro electro;
                if(firstElement instanceof Pyro){
                    pyro =(Pyro) firstElement;
                }
                else{
                    pyro = (Pyro) secondElement;
                }
                Vec3d playerPos = owner.getPos();
                Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                        .createExplosion(attackerElementContainer != null ? attackerElementContainer.getOwner() : null,
                                playerPos.x, playerPos.y + owner.getHeight()/2, playerPos.z, 1, World.ExplosionSourceType.NONE);

                DamageSourceHolder damageSourceHolder =(DamageSourceHolder) DamageSourceHolder.createDamageSource(owner.getWorld(),owner,attacker);
                damageSourceHolder.setDamageElement$EW(pyro);//这次攻击的元素类型为火元素
                damageSourceHolder.getEWDamageSource$EW().setCannotApply();//这次攻击没有元素附着

                owner.damage((DamageSource) damageSourceHolder,3);//对自己造成一个值为3的伤害
            }
            case SUPERCONDUCT -> {
                Vec3d pos = owner.getPos();
                final int range = 5;
                List<LivingEntity> entityList = owner.getWorld().getEntitiesByClass(
                        LivingEntity.class,
                        new Box(pos.x - range,pos.y - range,pos.z - range,pos.x +range,pos.y+range, pos.z + range),
                        livingEntity -> livingEntity instanceof LivingEntityHolder
                );
                //下面造成伤害并且降低抗性
                Modifier modifier = new Modifier("SuperConductModifier",0.4,240, Modifier.ModifierMethod.MULTI);//创建一个修改器实例
                //超导为冰元素伤害，且不造成元素附着
                DamageSource superConductSource = DamageSourceHolder.createDamageSource(owner.getWorld(), owner, attacker);
                ((DamageSourceHolder) superConductSource).setDamageElement$EW(Element.create(Element.ElementType.CRYO, 0));
                ((DamageSourceHolder) superConductSource).getEWDamageSource$EW().setCannotApply();

                for(LivingEntity entity : entityList){//遍历范围内的所有生物
                    if(entity == owner){
                        continue;//owner 在下方单独处理，避免重复结算
                    }
                    if(Calculator.distance(entity.getPos(),owner.getPos()) <= 5){
                        entity.damage(superConductSource,0.5f);
                        if(entity instanceof LivingEntityHolder holder){
                            holder.getElementContainer$EW()
                                    .getModifiers(Modifiers.ModifierType.RESISTANCE).addModifier(modifier);
                        }
                    }
                }
                //对本身也造成伤害并降低抗性
                owner.damage(superConductSource,0.5f);
                ownerElementContainer.getModifiers(Modifiers.ModifierType.RESISTANCE).addModifier(modifier);
            }
            default -> {
                ElementWorld.LOGGER.warn("Unhandled transformative reaction: {}", reactionType);
            }
        }
    }

}
