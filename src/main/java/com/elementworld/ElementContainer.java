package com.elementworld;

import com.elementworld.elementComponents.BonusContainer;
import com.elementworld.elementComponents.ResistancesContainer;
import com.elementworld.elementComponents.modifiers.Modifier;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elementComponents.reaction.Combustion;
import com.elementworld.elementComponents.reaction.ElectroCharged;
import com.elementworld.elementComponents.reaction.ElementReactionPriority;
import com.elementworld.elementComponents.reaction.Reaction;
import com.elementworld.elementComponents.reaction.ReactionContext;
import com.elementworld.elementComponents.reaction.ReactionHandler;
import com.elementworld.elementComponents.reaction.ReactionOutcome;
import com.elementworld.elementComponents.reaction.ReactionTable;
import com.elementworld.elementComponents.shield.Shield;
import com.elementworld.elements.Anemo;
import com.elementworld.elements.Catalyze;
import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import com.elementworld.elements.Frozen;
import com.elementworld.elements.Geo;
import com.elementworld.elements.Hydro;
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
        统一通过 reactions.containsKey 判定，消除布尔标志与 Map 的双重冗余读取。
         */
        if(isElectroCharged){
            HashMap<Class<? extends Reaction>, Reaction> reactionsMap = ensureReactionsMap();
            ElectroCharged electroCharged = (ElectroCharged) reactionsMap.get(ElectroCharged.class);
            if(electroCharged != null){
                if(electroCharged.die){
                    reactionsMap.remove(ElectroCharged.class);
                    isElectroCharged = false;
                }else {
                    electroCharged.tick();//运行tick方法
                }
            }else{
                //布尔标志与 Map 失同步的兜底：Map 中无实例则清标志
                isElectroCharged = false;
            }
        }
        if(isCombustion){
            HashMap<Class<? extends Reaction>, Reaction> reactionsMap = ensureReactionsMap();
            Combustion combustion = (Combustion) reactionsMap.get(Combustion.class);
            if(combustion != null){
                if(combustion.die){
                    reactionsMap.remove(Combustion.class);
                    isCombustion = false;
                }else {
                    combustion.tick();
                }
            }else{
                isCombustion = false;
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

        resolveReaction 是与 Damage 解耦的结算入口：完成元素附着 / 反应副作用，
        返回 ReactionOutcome 供伤害侧（LivingEntityMixin）决定是否改写伤害倍率。
        applyElement 作为返回旧 Reaction 的兼容包装保留（仅返回增幅/持续反应实例，
        其他情况返回 null），现有未迁移调用方仍可用。
     */
    public ReactionOutcome resolveReaction(@NotNull Element element, @Nullable LivingEntity attacker, @Nullable DamageSource damageSource){
        /*
        cannotApply 处理：反应自身引发的二级元素伤害（燃烧/超载/超导/扩散内层）会设 cannotApply，
        此类伤害不应再触发新一轮元素附着与反应结算，直接返回 NONE。
        伤害侧据此对 amount 走「有元素但非增幅」的增伤×减抗处理。
        注意：扩散（Swirl）对外层的 setElement 不设 cannotApply，是传染链设计，原样保留。
         */
        if(damageSource instanceof DamageSourceHolder holder){
            if(holder.getEWDamageSource$EW() != null && holder.getEWDamageSource$EW().isCannotApply()){
                return ReactionOutcome.NONE;
            }
        }

        //绑定 owner / attacker（旧 applyElement :208-212 行为）
        element.setOwner(owner);
        element.setAttacker(attacker);

        //更新容器的最后攻击者（反应触发者）
        latestAttacker = attacker;

        //获取攻击者的元素容器（旧 :218-224 行为）
        ElementContainer attackerElementContainer = null;
        if(attacker instanceof LivingEntityHolder livingEntityHolder){
            attackerElementContainer = livingEntityHolder.getElementContainer$EW();
        }

        Element.ElementType triggerType = Element.typeOfElementClass(element.getClass());
        if (triggerType == null) {
            return ReactionOutcome.NONE;
        }

        boolean reacted = false;
        ReactionOutcome finalOutcome = ReactionOutcome.NONE;
        int guard = 0;

        while (element.getGauge() > 0 && guard++ < 16) {
            removeDeadElements();

            Element aura = ElementReactionPriority.selectAura(triggerType, elements);
            if (aura == null) {
                break;
            }

            Element.ElementType auraType = Element.typeOfElementClass(aura.getClass());
            ReactionHandler handler = ReactionTable.get(triggerType, auraType);
            if (handler == null) {
                break;
            }

            double triggerBefore = element.getGauge();
            double auraBefore = aura.getGauge();
            boolean auraWasPresent = elements.contains(aura);

            ReactionContext ctx = new ReactionContext(
                    element, aura, owner, attacker, damageSource, this, attackerElementContainer);
            ReactionOutcome outcome = handler.handle(ctx);
            removeDeadElements();

            if (outcome != ReactionOutcome.NONE) {
                reacted = true;
                finalOutcome = mergeOutcome(finalOutcome, outcome);
            } else {
                return finalOutcome;
            }

            boolean progressed = element.getGauge() < triggerBefore
                    || aura.getGauge() < auraBefore
                    || (auraWasPresent && !elements.contains(aura));
            if (!progressed) {
                ElementWorld.LOGGER.warn("Element reaction made no gauge progress: trigger={}, aura={}",
                        triggerType, auraType);
                break;
            }
        }

        if (!reacted && element.getGauge() > 0 && !(element instanceof Anemo || element instanceof Geo)) {
            addElement(element);
        }

        removeDeadElements();
        return finalOutcome;
    }

    /**
     * 旧入口的兼容包装。返回 ReactionOutcome（新签名）。
     * 伤害侧调用方应改用 {@link #resolveReaction}。
     */
    public ReactionOutcome applyElement(@NotNull Element element, @Nullable DamageSource damageSource){
        LivingEntity attacker = damageSource != null ? (LivingEntity) damageSource.getAttacker() : null;
        return resolveReaction(element, attacker, damageSource);
    }



    /*
    private方法
     */
    // Element attachment with attach decay.
    private void addElement(Element element){
        Element existing = findElement(element.getClass());
        double attachedGauge = element.getGauge() * ReactionTable.ATTACH_DECAY;
        if (existing != null) {
            existing.setGauge(Math.max(existing.getGauge(), attachedGauge));
        } else {
            element.setGauge(attachedGauge);
            element.setOwner(owner);
            elements.add(element);
        }
        rebuildElementState();
    }

    // Remove an attached element.
    private void removeElement(Element element){
        elements.remove(element);
        rebuildElementState();
    }

    private Element findElement(Class<? extends Element> elementClass) {
        for (Element element : elements) {
            if (element.getClass() == elementClass) {
                return element;
            }
        }
        return null;
    }

    private Element copyElement(Element element) {
        Element.ElementType elementType = Element.typeOfElementClass(element.getClass());
        if (elementType == null) {
            return null;
        }
        Element copy = Element.create(elementType, element.getGauge());
        if (copy != null) {
            copy.setOwner(owner);
            copy.setAttacker(element.getAttacker());
        }
        return copy;
    }

    private void removeDeadElements() {
        deadElements.clear();
        for (Element element : elements) {
            if (element.getGauge() <= 0) {
                deadElements.add(element);
            }
        }
        for (Element element : deadElements) {
            removeElement(element);
        }
        deadElements.clear();
    }

    private ReactionOutcome mergeOutcome(ReactionOutcome current, ReactionOutcome next) {
        if (current instanceof ReactionOutcome.Amplified) {
            return current;
        }
        if (next instanceof ReactionOutcome.Amplified) {
            return next;
        }
        if (current == ReactionOutcome.NONE) {
            return next;
        }
        return current;
    }

    public void addIncomingElement(Element element){
        Element copy = copyElement(element);
        if (copy != null) {
            addElement(copy);
        }
    }
    /**
     * 移除附着元素（供 Electro×Pyro 超载的不对称「移除 aura」行为调用）。
     */
    public void removeAuraElement(Element aura){
        removeElement(aura);
    }

    /**
     * 添加冻结元素（无损耗，等价旧 applyElement 内的 elements.add(new Frozen(...))）。
     */
    public void addFrozenElement(Frozen frozen){
        Element existing = findElement(Frozen.class);
        if (existing != null) {
            existing.setGauge(Math.max(existing.getGauge(), frozen.getGauge()));
        } else {
            frozen.setOwner(owner);
            elements.add(frozen);
        }
        rebuildElementState();
    }
    /**
     * 添加激元素（无损耗，等价旧 applyElement 内的 elements.add(new Catalyze(...))）。
     */
    public void addCatalyzeElement(Catalyze catalyze){
        Element existing = findElement(Catalyze.class);
        if (existing != null) {
            existing.setGauge(Math.max(existing.getGauge(), catalyze.getGauge()));
        } else {
            catalyze.setOwner(owner);
            elements.add(catalyze);
        }
        rebuildElementState();
    }
    /**
     * 确保 reactions Map 已初始化并返回它。供 handler 注册持续反应时调用。
     */
    public HashMap<Class<? extends Reaction>, Reaction> ensureReactionsMap(){
        if(reactions == null){
            reactions = new HashMap<>();
        }
        return reactions;
    }

    /** 标记已注册感电（供 ReactionTable.registerElectroCharged 调用）。 */
    public void markElectroCharged(){
        isElectroCharged = true;
    }

    /** 标记已注册燃烧（供 ReactionTable.registerCombustion 调用）。 */
    public void markCombustion(){
        isCombustion = true;
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
        return findElement(c) != null;
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
