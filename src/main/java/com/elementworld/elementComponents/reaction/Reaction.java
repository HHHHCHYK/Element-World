package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public abstract class Reaction {
    protected final LivingEntity owner;
    protected final LivingEntity attacker;


    protected final Element firstElement;
    protected final Element secondElement;

    public final float damageValue;
    protected ReactionType reactionType;
    protected DamageSource damageSource;

    public Reaction(LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement){

        //标定拥有者，施加者，还有参与反应的两个元素实例
        this.owner = owner;
        this.damageSource = damageSource;
        if(damageSource != null){
            if(damageSource.getAttacker() instanceof LivingEntity){
                this.attacker =(LivingEntity) damageSource.getAttacker();
            }
            else {
                attacker = null;
            }
        }else{
            attacker = null;
        }

        //初始化反应伤害
        /*
        这里需要非空检查
         */
        if(secondELement.getOwnerContainer() == null) {
            damageValue = 0.6f;
        }
        else{
            damageValue = (float) (0.6*((1+secondELement.getOwnerContainer().getMasteryBonus())));
        }
        this.firstElement = firseElement;
        this.secondElement = secondELement;
    }

    public Reaction(ReactionType reactionType,LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement){

        this.reactionType = reactionType;

        //标定拥有者，施加者，还有参与反应的两个元素实例
        this.owner = owner;

        if(damageSource.getAttacker() instanceof LivingEntity){
            this.attacker =(LivingEntity) damageSource.getAttacker();
        }
        else {
            attacker = null;
        }

        //初始化反应伤害
        damageValue = (float) (0.6*((1+secondELement.getOwnerContainer().getMasteryBonus())));
        this.firstElement = firseElement;
        this.secondElement = secondELement;
    }

    /**
     * 原神元素反应枚举（英文名称）
     */
    public enum ReactionType {
        // 增幅反应 (Amplifying Reactions)
        VAPORIZE,       // 蒸发
        MELT,           // 融化

        // 剧变反应 (Transformative Reactions)
        OVERLOAD,       // 超载
        ELECTRO_CHARGED, // 感电
        SUPERCONDUCT,   // 超导
        SHATTER,        // 碎冰
        COMBUSTION,     // 燃烧
        SWIRL,          //扩散

                // 绽放反应 (Bloom Reactions)
        BLOOM,          // 原绽放
        BURGEON,        // 烈绽放
        HYPERBLOOM,     // 超绽放

        // 其他反应 (Other Reactions)

        CRYSTALLIZE,    // 结晶
        CATALYZE        // 激化（包含蔓激化/超激化）
    }

    public void apply(){
    }

    public static final ReactionType[] ampReaction= {ReactionType.VAPORIZE,ReactionType.MELT};
    public static final ReactionType[] tfReaction = {
            ReactionType.OVERLOAD,ReactionType.ELECTRO_CHARGED,ReactionType.SUPERCONDUCT,
            ReactionType.SHATTER,ReactionType.COMBUSTION
    };
    public static final ReactionType[] bloomReaction = {ReactionType.BLOOM,ReactionType.BURGEON,ReactionType.HYPERBLOOM};
    public static final ReactionType[] otherReaction = {ReactionType.CRYSTALLIZE,ReactionType.CATALYZE};

    public static ReactionType[] getReactionGroup(ReactionType type){
        switch (type){
            case CATALYZE,CRYSTALLIZE -> {
                return otherReaction;
            }
            case COMBUSTION, SWIRL, OVERLOAD, ELECTRO_CHARGED, SUPERCONDUCT, SHATTER -> {
                return tfReaction;
            }
            case HYPERBLOOM,BLOOM,BURGEON -> {
                return bloomReaction;
            }
            case MELT,VAPORIZE -> {
                return ampReaction;
            }
            default -> {
                return null;
            }
        }
    }

    public static Reaction create(ReactionType reactionType,LivingEntity owner, DamageSource damageSource, Element firseElement, Element secondELement){
        switch (reactionType){
            case CATALYZE -> {
                return new Catalyze(reactionType,owner, damageSource, firseElement, secondELement);
            }
            case CRYSTALLIZE -> {
                return new Crystallize(owner, damageSource, firseElement, secondELement);
            }
            case OVERLOAD,SUPERCONDUCT,SHATTER -> {
                return new TFReaction(reactionType,owner, damageSource, firseElement, secondELement);
            }
            case SWIRL -> {
                return new Swirl(owner, damageSource, firseElement, secondELement);
            }
            case HYPERBLOOM,BLOOM,BURGEON -> {
                return new BloomReaction(reactionType,owner, damageSource, firseElement, secondELement);
            }
            case MELT,VAPORIZE -> {
                return new AmpReaction(reactionType,owner, damageSource, firseElement, secondELement);
            }
            case COMBUSTION -> {
                return new Combustion(owner, damageSource, firseElement, secondELement);
            }
            case ELECTRO_CHARGED -> {
                return new Electro_Charged(owner, damageSource, firseElement, secondELement);
            }
            default -> {
                return null;
            }
        }
    }
}
