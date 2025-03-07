package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Cryo;
import com.elementworld.elements.Element;
import com.elementworld.elements.Hydro;
import com.elementworld.elements.Pyro;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class AmpReaction extends Reaction{

    public AmpReaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement) {
        super(reactionType, owner, damageSource, firseElement, secondELement);
    }

    public static double getMasteryAmp(double mastery){
        return 1+((2.78*mastery)/(mastery+1400));
    }

    public double getReactionBaseMul(){
        if(reactionType == ReactionType.VAPORIZE){
            if(secondElement instanceof Hydro && firstElement instanceof Pyro){
                return 1;
            } else if (secondElement instanceof Pyro && firstElement instanceof Hydro) {
                return 0.5;
            }else {
                System.out.println("Wrong Element in Reaction");
                return -1;
            }
        } else if (reactionType == ReactionType.MELT) {
            if(secondElement instanceof Pyro && firstElement instanceof Cryo){
                return 1;
            } else if (secondElement instanceof Cryo || firstElement instanceof Pyro) {
                return 0.5;
            }else {
                System.out.println("Wrong Element in Reaction");
                return -1;
            }
        }else{
            System.out.println("Wrong Reaction Type");
            return -1;
        }
    }
}
