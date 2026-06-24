package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementWorld;
import com.elementworld.elements.Cryo;
import com.elementworld.elements.Element;
import com.elementworld.elements.Hydro;
import com.elementworld.elements.Pyro;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class AmpReaction extends Reaction{

    public AmpReaction(ReactionType reactionType, LivingEntity owner, DamageSource damageSource, Element firstElement, Element secondElement) {
        super(reactionType, owner, damageSource, firstElement, secondElement);
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
                ElementWorld.LOGGER.warn("Wrong element pair for VAPORIZE: first={}, second={}",
                        firstElement.getClass().getSimpleName(), secondElement.getClass().getSimpleName());
                return -1;
            }
        } else if (reactionType == ReactionType.MELT) {
            if(secondElement instanceof Pyro && firstElement instanceof Cryo){
                return 1;
            } else if (secondElement instanceof Cryo && firstElement instanceof Pyro) {
                return 0.5;
            }else {
                ElementWorld.LOGGER.warn("Wrong element pair for MELT: first={}, second={}",
                        firstElement.getClass().getSimpleName(), secondElement.getClass().getSimpleName());
                return -1;
            }
        }else{
            ElementWorld.LOGGER.warn("Wrong reaction type for AmpReaction: {}", reactionType);
            return -1;
        }
    }
}
