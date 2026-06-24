package com.elementworld;

import com.elementworld.elementComponents.BonusContainer;
import com.elementworld.elementComponents.ResistancesContainer;
import com.elementworld.elementComponents.modifiers.Modifier;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elementComponents.reaction.Combustion;
import com.elementworld.elementComponents.reaction.ElectroCharged;
import com.elementworld.elementComponents.reaction.Reaction;
import com.elementworld.elementComponents.shield.Shield;
import com.elementworld.elements.Anemo;
import com.elementworld.elements.Catalyze;
import com.elementworld.elements.Cryo;
import com.elementworld.elements.Dendro;
import com.elementworld.elements.EP;
import com.elementworld.elements.Electro;
import com.elementworld.elements.Element;
import com.elementworld.elements.Frozen;
import com.elementworld.elements.Geo;
import com.elementworld.elements.Hydro;
import com.elementworld.elements.Pyro;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import com.elementworld.persistence.ElementContainerState;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class ElementContainer {
//一些成员
    private final ArrayList<Element> elements = new ArrayList<>();//该映射用于存储该容器所拥有的元素实例
    private final LivingEntity owner;//此为该容器拥有者
    private LivingEntity latestAttacker;//最后施加元素的生物
    private HashSet<Modifiers> modifiersSet;//修改器集
    private HashMap<Class<?extends Reaction>,Reaction> reactions;
    private int displayRateCD = 0;

    //正常生存可以调整的模式
    private boolean displayElement = true;

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
    private final ArrayList<Element> deadElements = new ArrayList<>();//这是元素量小于0的元素的集合



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


        // Show attached elements while debug mode is enabled.
        if (ElementWorld.DEBUG && owner instanceof PlayerEntity player){
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
            if(!hasCatalyze &&element instanceof Catalyze){
                hasCatalyze = true;
            }
            hasElement.put(element.getClass(),true);
        }
        if(hasCatalyze && !isCatalyze){
            isCatalyze = true;
        }

        /*
        检查感电状态和燃烧状态
         */
        if(isElectroCharged){
            if(reactions == null){
                reactions = new HashMap<>();
            }
            ElectroCharged electroCharged =(ElectroCharged) reactions.get(ElectroCharged.class);
            if(electroCharged != null){
                if(electroCharged.die){
                    reactions.remove(ElectroCharged.class);
                    isElectroCharged=false;
                }else {
                    electroCharged.tick();//运行tick方法
                }
            }
        }
        if(isCombustion){
            if(reactions == null){
                reactions = new HashMap<>();
            }
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

        //------------------下面向客户端发送元素信息--------------------------------
        if(displayElement && owner instanceof ServerPlayerEntity player){
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());//创建一个数据包

            int size = elements.size();
            buf.writeInt(size);
            for(Element element : elements){
                buf.writeString(element.toString());
            }

            ServerPlayNetworking.send(player, ElementWorld.ELEMENT_TYPES_PACKET_ID, buf);
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
                        if(reactions.get(ElectroCharged.class) == null){
                            Reaction reaction = Reaction.create(Reaction.ReactionType.ELECTRO_CHARGED,owner,damageSource,bRElement,element);
                            reactions.put(ElectroCharged.class,reaction);
                            isElectroCharged = true;
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
                    ElementWorld.LOGGER.warn("ElementApplied Error: Hydro applied to unexpected element {}", bRElement.getClass().getSimpleName());
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
                    ElementWorld.LOGGER.warn("ElementApplied Error: Pyro applied to unexpected element {}", bRElement.getClass().getSimpleName());
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
                        if(reactions == null){
                            reactions = new HashMap<>();
                        }
                        reactions.put(ElectroCharged.class,reaction);
                        return reaction;
                    }
                }
                else if(bRElement instanceof Cryo || bRElement instanceof Frozen){//冰：超导
                    addElement(element);
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
                        elements.add(new Catalyze(min,getOwner(),element.getAttacker(),damageSource.getSource()));
                    }else {
                        elements.add(new Catalyze(min,getOwner(),null,owner));
                    }
                    return Reaction.create(Reaction.ReactionType.CATALYZE,owner,damageSource,bRElement,element);
                }
                else{
                    ElementWorld.LOGGER.warn("ElementApplied Error: Electro applied to unexpected element {}", bRElement.getClass().getSimpleName());
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
                        elements.add(new Catalyze(min,getOwner(),element.getAttacker(),damageSource.getSource()));
                    }else {
                        elements.add(new Catalyze(min,getOwner(),null,owner));
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
                ElementWorld.LOGGER.warn("Container has a wrong element type: {}", element.getClass().getSimpleName());
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

    private void rebuildElementState() {
        hasElement = new HashMap<>();
        isWet = false;
        isCatalyze = false;
        for (Element element : elements) {
            hasElement.put(element.getClass(), true);
            if (element instanceof Hydro) {
                isWet = true;
            }
            if (element instanceof Catalyze) {
                isCatalyze = true;
            }
        }
    }

    private void clearRuntimeReactionState() {
        latestAttacker = null;
        reactions = null;
        isCombustion = false;
        isElectroCharged = false;
    }



    /*
    public方法
     */

    public ElementContainerState toPersistentState() {
        List<ElementContainerState.ElementEntry> elementEntries = new ArrayList<>();
        for (Element element : elements) {
            Element.ElementType elementType = Element.typeOfElementClass(element.getClass());
            if (elementType != null) {
                elementEntries.add(new ElementContainerState.ElementEntry(elementType, element.getGauge()));
            }
        }

        List<Class<? extends EP>> immuneElements = new ArrayList<>(immuneSet);

        List<ElementContainerState.BonusEntry> bonusEntries = new ArrayList<>();
        for (BonusContainer.BonusInstance bonus : bonusContainer.getBonusInstances()) {
            bonusEntries.add(new ElementContainerState.BonusEntry(
                    bonus.elementType(),
                    bonus.bonusValue(),
                    bonus.name()
            ));
        }

        List<ElementContainerState.ResistanceEntry> resistanceEntries = new ArrayList<>();
        for (ResistancesContainer.ResistanceInstance resistance : resistancesContainer.getResistanceInstances()) {
            resistanceEntries.add(new ElementContainerState.ResistanceEntry(
                    resistance.elementType(),
                    resistance.resistanceValue(),
                    resistance.name()
            ));
        }

        List<ElementContainerState.ModifierGroup> modifierGroups = new ArrayList<>();
        if (modifiersSet != null) {
            for (Modifiers modifiers : modifiersSet) {
                List<ElementContainerState.ModifierEntry> modifierEntries = new ArrayList<>();
                for (Modifier modifier : modifiers.getModifiers()) {
                    if (!modifier.isDie()) {
                        modifierEntries.add(new ElementContainerState.ModifierEntry(
                                modifier.getName(),
                                modifier.getValue(),
                                modifier.getDuration(),
                                modifier.getMethod()
                        ));
                    }
                }
                modifierGroups.add(new ElementContainerState.ModifierGroup(modifiers.getType(), modifierEntries));
            }
        }

        List<ElementContainerState.ShieldEntry> shieldEntries = new ArrayList<>();
        if (shields != null) {
            for (Shield shield : shields.values()) {
                if (!shield.isDie()) {
                    shieldEntries.add(new ElementContainerState.ShieldEntry(
                            shield.getName(),
                            shield.getValue(),
                            shield.getMaxValue(),
                            shield.getElement()
                    ));
                }
            }
        }

        return new ElementContainerState(
                elementEntries,
                mastery,
                displayElement,
                shieldStrength,
                immuneElements,
                bonusEntries,
                resistanceEntries,
                modifierGroups,
                shieldEntries
        );
    }

    public void loadPersistentState(ElementContainerState state) {
        elements.clear();
        deadElements.clear();
        clearRuntimeReactionState();

        for (ElementContainerState.ElementEntry elementEntry : state.elements()) {
            Element element = Element.create(elementEntry.type(), elementEntry.gauge());
            if (element != null) {
                element.bindOwner(owner);
                elements.add(element);
            }
        }

        mastery = state.mastery();
        displayElement = state.displayElement();
        shieldStrength = state.shieldStrength();

        immuneSet.clear();
        immuneSet.addAll(state.immuneElements());

        bonusContainer.clear();
        for (ElementContainerState.BonusEntry bonus : state.bonuses()) {
            bonusContainer.addBonus(bonus.elementType(), bonus.value(), bonus.name());
        }

        resistancesContainer.clear();
        for (ElementContainerState.ResistanceEntry resistance : state.resistances()) {
            resistancesContainer.addResistance(resistance.elementType(), resistance.value(), resistance.name());
        }

        modifiersSet = null;
        for (ElementContainerState.ModifierGroup modifierGroup : state.modifiers()) {
            Modifiers modifiers = getModifiers(modifierGroup.type());
            for (ElementContainerState.ModifierEntry modifierEntry : modifierGroup.entries()) {
                modifiers.addModifier(new Modifier(
                        modifierEntry.name(),
                        modifierEntry.value(),
                        modifierEntry.duration(),
                        modifierEntry.method()
                ));
            }
        }

        shields = null;
        for (ElementContainerState.ShieldEntry shieldEntry : state.shields()) {
            Shield shield = shieldEntry.elementType() == null
                    ? new Shield(shieldEntry.name(), owner, shieldEntry.maxValue())
                    : new Shield(shieldEntry.name(), owner, shieldEntry.maxValue(), shieldEntry.elementType());
            shield.setValue(shieldEntry.value());
            addShield(shield);
        }

        rebuildElementState();
        // Loaded attached elements are passive state; continuous reactions must be created by future gameplay.
        clearRuntimeReactionState();
    }

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
    public Modifiers getModifiers(Modifiers.ModifierType modifierType){
        if(modifiersSet == null){
            modifiersSet = new HashSet<>();
        }

        for(Modifiers modifiers : modifiersSet){
            if(modifiers.getType() == modifierType){
                return modifiers;
            }
        }
        Modifiers newModifiers = new Modifiers(modifierType);
        modifiersSet.add(newModifiers);
        return newModifiers;
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
        return getModifiers(Modifiers.ModifierType.MASTERY).applyModifiers((float) getBaseMastery());
    }

    public double getMasteryBonus(){
        return (16*getMastery())/(getMastery()+2000);
    }

    public void setImmune(Class<? extends EP> eop){//设置免疫类型
        immuneSet.add(eop);
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

    //------------------------下面实现护盾运算----------------------------------
    protected LinkedHashMap<UUID, Shield> shields;
    private double shieldStrength = 1;

    public double getShieldStrength(){
        return shieldStrength;
    }

    public void setShieldStrength(double shieldStrength){
        this.shieldStrength = shieldStrength;
    }

    public void addShield(@NotNull Shield shield){
        if(shields == null){
            shields = new LinkedHashMap<>();
        }


        if (shields.containsKey(shield.getUuid())) {
            Shield shield1 = shields.get(shield.getUuid());
            shield1.recover(shield.getValue());
        } else {
            shields.put(shield.getUuid(), shield);
        }
    }

    /*
    该方法用于计算护盾对伤害的抵消
    返回值：经过护盾抵消后剩余的伤害（没有护盾或护盾未满格时原样/部分返回）
     */
    public float applyShield(@Nullable Class<? extends Element> element,float damage){
        if(shields == null || shields.isEmpty()){
            return damage;
        }
        //清理已失效的护盾
        shields.values().removeIf(Shield::isDie);
        for(Shield shield : shields.values()){
            if(damage <= 0){
                break;
            }
            damage = (float) shield.apply(damage, element);
        }
        return Math.max(damage, 0);
    }
}
