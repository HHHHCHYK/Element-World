package com.elementworld;

import com.elementworld.elementComponents.BonusContainer;
import com.elementworld.elementComponents.ResistancesContainer;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elementComponents.reaction.Combustion;
import com.elementworld.elementComponents.reaction.Electro_Charged;
import com.elementworld.elementComponents.reaction.Reaction;
import com.elementworld.elements.*;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElementContainer {

    /*
    //创建一个类的实例方便调用
    public static final Anemo ANEMO = (Anemo) Element.create(Element.ElementType.ANEMO,0);
    public static final Cryo CRYO = (Cryo) Element.create(Element.ElementType.CRYO,0);
    public static final Dendro DENDRO = (Dendro) Element.create(Element.ElementType.DENDRO,0);
    public static final Electro ELECTRO = (Electro) Element.create(Element.ElementType.ELECTRO,0);
    public static final Frozen FROZEN = (Frozen) Element.create(Element.ElementType.FROZEN,0);
    public static final Geo GEO = (Geo) Element.create(Element.ElementType.GEO,0);
    public static final Hydro HYDRO = (Hydro) Element.create(Element.ElementType.HYDRO,0);
    public static final Pyro PYRO = (Pyro) Element.create(Element.ElementType.PYRO,0);
    public static final Quicken QUICKEN = (Quicken) Element.create(Element.ElementType.Quicken,0);

     */

    //一些成员
    private final ArrayList<Element> elements = new ArrayList<>();//该映射用于存储该容器所拥有的元素实例
    private final LivingEntity owner;//此为该容器拥有者
    private LivingEntity latestAttacker;//最后施加元素的生物
    private HashSet<Modifiers> modifiersSet;//修改器集
    private HashMap<Class<?extends Reaction>,Reaction> reactions;

    //!Debug!
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean debugMode = true;//此处打开debug模式，等到正式发布时删除（将此处变量设为false）这部分代码
    private int displayRateCD = 0;

    //正常生存可以调整的模式
    public boolean displayElement = false;//是否显示元素（这里应该是是否显示图标，与上方debug模式作区分）

    //owner的各类属性
    private double mastery;//元素精通
    private final BonusContainer bonusContainer;//增伤容器
    private final ResistancesContainer resistancesContainer;//抗性容器
    private final HashSet<Class<? extends EP>> immuneSet = new HashSet<>();


    //owner的状态
    private boolean isCombustion = false;
    private boolean isElectroCharged = false;
    private boolean isCatalyze = false;
    private boolean isWet = false;

    //辅助数据结构
    private HashMap<Class<? extends Element>,Boolean> hasElement;//此集合用于表示是否有某种元素
    private final Vector<Element> deadElements = new Vector<>();//这是元素量小于0的元素的集合



    //构造函数
    public ElementContainer(LivingEntity owner){
        this.owner = owner;
        bonusContainer = new BonusContainer(owner);
        resistancesContainer = new ResistancesContainer(owner);
    }

    public void tick(){
        /*
        这里放置其他类的tick方法
         */
        if(modifiersSet != null){
            for(Modifiers modifiers : modifiersSet){
                modifiers.tick();
            }
        }


        for (Element element : elements) {
            /*
            下面运行每个附着元素的tick方法
             */
            element.tick();
            /*
            元素量小于等于零的元素标记为需要移除
             */
            if(element.getGauge()<=0){
                deadElements.add(element);
            }
        }

        //----------------上面为tick方法-----------------------------------------------


        //统一移除被标记的元素
        for(Element element : deadElements){
            removeElement(element);
        }
        deadElements.clear();//清空这个tick需要清除的元素


        //Debug模式：显示玩家目前所拥有的元素
        if(owner instanceof PlayerEntity player && debugMode){
            boolean flag = true;
            StringBuilder stringBuilder = new StringBuilder();
            for(Element element : elements){
                String gauge = String.format("%.2f",element.getGauge());
                if(flag){
                    flag = false;
                    stringBuilder.append(element).append(" ").append(gauge);
                }
                else {
                    stringBuilder.append(",").append(element).append(" ").append(gauge);
                }
            }
            if(displayRateCD > 0){
                displayRateCD--;
            }
            else{
                displayRateCD = 5;
                player.sendMessage(Text.literal(stringBuilder.toString()),true);
            }
        }

        //懒加载这个映射
        if(hasElement == null){hasElement = new HashMap<>();}

        boolean hasCatalyze = false;
        //将其中元素的状态更新
        for(Element element : elements){
            if(!hasCatalyze &&element instanceof Quicken){
                hasCatalyze = true;
            }
            hasElement.put(element.getClass(),true);
        }
        if(hasCatalyze && !isCatalyze){
            isCatalyze = true;
        }

        /*

         */
        if(isElectroCharged){
            Electro_Charged electroCharged =(Electro_Charged) reactions.get(Electro_Charged.class);
            if(electroCharged != null){
                if(electroCharged.die){
                    reactions.remove(Electro_Charged.class);
                    isElectroCharged=false;
                }else {
                    electroCharged.tick();//运行tick方法
                }
            }
        }
        if(isCombustion){
            Combustion combustion = (Combustion) reactions.get(Combustion.class);
            if(combustion != null){
                if(combustion.die){
                    reactions.remove(Combustion.class);
                    isCombustion = false;
                }else {
                    combustion.tick();
                }
            }
        }
    }

    /*
        下面处理产生元素附着的情况
     */
    public Reaction applyElement(@NotNull Element element, @Nullable DamageSource damageSource){
        if (damageSource != null) {
            LivingEntity attacker =(LivingEntity) damageSource.getAttacker();
            element.setOwner(owner);
            element.setAttacker(attacker);
        }

        //更新容器的最后攻击者(反应触发者）
        latestAttacker = element.getAttacker();

        //获取攻击者的元素精通和元素反应容器
        ElementContainer attackerElementContainer;
        if(element.getOwner() instanceof LivingEntityHolder livingEntityHolder){
            attackerElementContainer = livingEntityHolder.getElementContainer$EW();
        }
        else{
            attackerElementContainer = null;
        }
        /*
         集合为空
         */
        if (elements.isEmpty()) {
            if(!(element instanceof Anemo || element instanceof Geo)){//风岩不附着
                addElement(element);
            }
        }
        /*
        如果只有一个元素被附着
         */
        else if (elements.size() == 1) {
            Element bRElement = elements.iterator().next();



            double gauge = element.getGauge();//后手元素元素量
            double beGauge = bRElement.getGauge();//附着元素元素量

            if (bRElement.getClass() == element.getClass()) {
                bRElement.setGauge(Math.max(gauge, beGauge));
            }

            //如若被添加元素为水元素
            if (element instanceof Hydro) {
                if (bRElement instanceof Pyro) {//火
                    bRElement.subGauge(gauge * 2);
                    //返回反应为蒸发反应
                    return Reaction.create(Reaction.ReactionType.VAPORIZE,owner,damageSource,bRElement,element);
                } else if (bRElement instanceof Cryo) {//冰！
                    //此处生成冻元素
                    elements.add(new Frozen(Math.min(gauge, beGauge) * 2));
                    bRElement.subGauge(gauge);
                    element.subGauge(bRElement.getGauge());
                    //不返回反应（只是生成冻元素)
                }
                else if(bRElement instanceof Frozen){//冻
                    addElement(element);
                    //未发生反应
                }
                else if (bRElement instanceof Element) {//雷
                    addElement(element);
                    if (attackerElementContainer != null) {
                        if(reactions == null){
                            reactions = new HashMap<>();
                        }
                        if(reactions.get(Electro_Charged.class) == null){
                            Reaction reaction = Reaction.create(Reaction.ReactionType.ELECTRO_CHARGED,owner,damageSource,bRElement,element);
                            reactions.put(Electro_Charged.class,reaction);
                            isElectroCharged = true;
                            addElement(element);
                            //返回感电反应
                            return reaction;
                        }
                    }
                }
                else if (bRElement instanceof Dendro) {//草
                    /*
                    这里缺少草反应相关逻辑（还没想好草反应怎么写）
                     */
                    bRElement.subGauge(gauge);
                    return Reaction.create(Reaction.ReactionType.BLOOM,owner,damageSource,bRElement,element);
                }
                else {
                    System.out.println("[Warning] ElementApplied Error!");
                }
            }

            //如若添加的元素为火
            if (element instanceof Pyro) {
                if (bRElement instanceof Hydro) {//水
                    bRElement.subGauge(gauge * 0.5);
                    return Reaction.create(Reaction.ReactionType.VAPORIZE,owner,damageSource,bRElement,element);
                }
                else if (bRElement instanceof Electro) {//雷
                    bRElement.subGauge(element.getGauge());
                    return Reaction.create(Reaction.ReactionType.OVERLOAD,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Cryo){//冰！！
                    bRElement.subGauge(gauge*2);
                    return Reaction.create(Reaction.ReactionType.MELT,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Frozen){
                    bRElement.subGauge(gauge*2);
                    return Reaction.create(Reaction.ReactionType.MELT,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Dendro){//燃烧（火 to 草）
                    addElement(element);
                    if(reactions == null){
                        reactions = new HashMap<>();
                    }
                    if(reactions.get(Combustion.class) == null){
                        Reaction reaction = Reaction.create(Reaction.ReactionType.COMBUSTION,owner,damageSource,bRElement,element);
                        reactions.put(Combustion.class,reaction);
                        isCombustion = true;
                        return reaction;
                    }
                }
                else{
                    System.out.println("[Warning] ElementApplied Error!");
                }
            }
            //如果添加的元素为雷
            if(element instanceof Electro){
                if(bRElement instanceof Pyro){//火：超载
                    removeElement(bRElement);
                    return Reaction.create(Reaction.ReactionType.OVERLOAD,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Hydro){//水：感电
                    addElement(element);
                    if(!isElectroCharged){
                        isElectroCharged = true;
                        Reaction reaction = Reaction.create(Reaction.ReactionType.ELECTRO_CHARGED,owner,damageSource,bRElement,element);
                        reactions.put(Electro_Charged.class,reaction);
                        return reaction;
                    }
                }
                else if(bRElement instanceof Cryo || bRElement instanceof Frozen){//冰：超导
                    addElement(element);
                    /*
                    *
                    *
                    这里实现在某个范围内造成一次伤害
                    *
                    *
                    Vec3d pos = getOwner().getPos();
                    final int range = 5;
                    List<LivingEntity> entityList = getOwner().getWorld().getEntitiesByClass(
                            LivingEntity.class,
                            new Box(pos.x - range,pos.y - range,pos.z - range,pos.x +range,pos.y+range, pos.z + range),
                            livingEntity -> livingEntity instanceof LivingEntityHolder
                    );
                    //下面造成伤害并且降低抗性
                    Modifier modifier = new Modifier("SuperConductModifier",0.4,240, Modifier.modifierMethod.MULTI);
                    for(LivingEntity entity : entityList){
                        if(Calculater.distance(entity.getPos(),getOwner().getPos()) <= 5){
                            entity.damage(null,0.5f);
                            if(entity instanceof LivingEntityHolder holder){
                                holder.getElementContainer$EW()
                                        .getModifiers(Modifiers.modifierType.RESISTANCE).addModifier(modifier);
                            }
                        }
                    }
                    getOwner().damage(null,0.5f);

                    this.getModifiers(Modifiers.modifierType.RESISTANCE).addModifier(modifier);
                    *
                    *
                    */
                    return Reaction.create(Reaction.ReactionType.SUPERCONDUCT,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Dendro){//激化
                    addElement(element);
                    double min = Math.min(gauge, beGauge);
                    bRElement.subGauge(min);
                    element.subGauge(min);
                    /*
                    生成激元素
                     */
                    if (damageSource != null) {
                        elements.add(new Quicken(min,getOwner(),element.getAttacker(),damageSource.getSource()));
                    }else {
                        elements.add(new Quicken(min,getOwner(),null,owner));
                    }
                    return Reaction.create(Reaction.ReactionType.CATALYZE,owner,damageSource,bRElement,element);
                }
                else{
                    System.out.println("[Warning] ElementApplied Error!");
                }
            }
            else if(element instanceof Cryo){//后手元素为冰
                if(bRElement instanceof Hydro){//冰水冻结
                    bRElement.subGauge(gauge);
                    element.subGauge(beGauge);
                    elements.add(new Frozen(Math.min(gauge, beGauge) * 2));
                }
                else if(bRElement instanceof Pyro){//冰火融化
                    bRElement.subGauge(gauge*0.5);
                    return Reaction.create(Reaction.ReactionType.MELT,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Electro){//冰雷超导
                    addElement(element);
                    return Reaction.create(Reaction.ReactionType.SUPERCONDUCT,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Dendro){//冰草不反应
                    addElement(element);
                }
            }
            else if(element instanceof Dendro){//后手元素为草
                if(bRElement instanceof Hydro){//水草绽放
                    return Reaction.create(Reaction.ReactionType.BLOOM,owner,damageSource,bRElement,element);
                }
                else if(bRElement instanceof Pyro){//草火燃烧
                    addElement(element);
                    if(reactions == null){
                        reactions = new HashMap<>();
                    }
                    if(reactions.get(Combustion.class) == null){
                        Reaction reaction = Reaction.create(Reaction.ReactionType.COMBUSTION,owner,damageSource,bRElement,element);
                        reactions.put(Combustion.class,reaction);
                        isCombustion = true;
                        return reaction;
                    }
                }
                else if(bRElement instanceof Electro){//草雷激化
                    addElement(element);
                    double min = Math.min(gauge, beGauge);
                    bRElement.subGauge(min);
                    element.subGauge(min);
                    /*
                    生成激元素
                     */
                    if (damageSource != null) {
                        elements.add(new Quicken(min,getOwner(),element.getAttacker(),damageSource.getSource()));
                    }else {
                        elements.add(new Quicken(min,getOwner(),null,owner));
                    }
                    return Reaction.create(Reaction.ReactionType.CATALYZE,owner,damageSource,bRElement,element);
                }
            }
            else if(element instanceof Anemo){//后手风
                return Reaction.create(Reaction.ReactionType.SWIRL,owner,damageSource,bRElement,element);
            }
            else if(element instanceof Geo){//后手岩
                return Reaction.create(Reaction.ReactionType.CRYSTALLIZE,owner,damageSource,bRElement,element);
            }
            else{
                System.out.println("Container has a wrong element");
            }
        }
        return null;
    }



    /*
    private方法
     */
    //这个是带有损耗的元素附着模式
    private void addElement(Element element){
        element.setGauge(element.getGauge()*0.8);//元素附着损耗
        if(element instanceof Hydro){
            isWet = true;
        }
        elements.add(element);
    }

    //移除对应元素
    private void removeElement(Element element){
        elements.remove(element);

        //懒加载这个映射
        if(hasElement == null){hasElement = new HashMap<>();}
        //当被移除的时候，将存在映射设置为不存在
        hasElement.put(element.getClass(),false);
        if(element instanceof Hydro){
            isWet = false;
        }
    }

    /*
    下面实现超载：
     超载反应特征：爆炸并且造成伤害
        先获取容器拥有者的坐标位置和服务器世界实例，
      然后在坐标位置生成一个有伤害的爆炸
    */

    private void overload(ElementContainer attackerElementContainer){
        Vec3d playerPos = owner.getPos();
        Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                .createExplosion(attackerElementContainer.getOwner(), playerPos.x, playerPos.y + owner.getHeight()/2, playerPos.z, 1, World.ExplosionSourceType.NONE);
        owner.damage(createDamageSource(), (float) (3 * (1 + attackerElementContainer.getMasteryBonus())));
    }

    /*
    public方法
     */

    public boolean isEmpty(){
        return elements.isEmpty();
    }

    public boolean has(Class<? extends Element> c){
        if(hasElement == null){
            return false;
        }
        if (hasElement.get(c) == null){
            return false;
        }
        return hasElement.get(c);
    }

    public boolean isImmune(Class<? extends EP> eop){
        return immuneSet.contains(eop);
    }

    /*
    以下为getter&setter
     */


    //下面这个方法用于get修改器列表
    public Modifiers getModifiers(Modifiers.modifierType modifierType){
        if(modifiersSet == null){
            modifiersSet = new HashSet<>();
        }

        for(Modifiers modifiers : modifiersSet){
            if(modifiers.getType() == modifierType){
                return modifiers;
            }
        }
        modifiersSet.add(new Modifiers(modifierType));
        return null;
    }

    /*
    获取对于元素的增伤值
     */
    public double getBonusValue(Class<?extends EP> eop){
        return bonusContainer.getBonusValue(eop);
    }

    public double getResistanceValue(Class<?extends EP> eop){
        return resistancesContainer.getResistanceValue(eop);
    }


    public boolean isCombustion(){
        return isCombustion;
    }

    public boolean isElectroCharged(){
        return isElectroCharged;
    }

    public Collection<Element> getElements(){
        return elements;
    }

    public boolean isWet() {
        return isWet;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public double getBaseMastery(){
        return mastery;
    }

    public double getMastery(){
        return getModifiers(Modifiers.modifierType.MASTERY).applyModifiers((float) getBaseMastery());
    }

    public double getMasteryBonus(){
        return (16*getMastery())/(getMastery()+2000);
    }


    /*
    该方法创建的伤害类型为持续反应伤害，此处source是玩家本身，attacker是最后施加元素的生物
     */
    private DamageSource createDamageSource(){
        return DamageSourceHolder.createDamageSource(owner.getWorld(),owner,latestAttacker);
    }


    //下面的方法已经弃置
    /*
    该方法已弃置（不再维护），弃置原因：对于伤害的处理改为通过Mixin修改原本方法
     */
    @Deprecated
    private static DamageSource applyDamage(LivingEntity target,LivingEntity source,LivingEntity attacker,float amount,Class<? extends Element> elementType){
        Registry<DamageType> damageTypeRegistry = target.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        DamageType type = damageTypeRegistry.get(ElementWorld.ELEMENT_DAMAGE);

        DamageSource damageSource = new DamageSource(damageTypeRegistry.getEntry(type),source,attacker);
        target.damage(damageSource,amount);

        return damageSource;
    }


    public ResistancesContainer getResistancesContainer() {
        return resistancesContainer;
    }

    public BonusContainer getBonusContainer() {
        return bonusContainer;
    }
}
