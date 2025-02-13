package com.elementworld;

import com.elementworld.elements.*;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElementContainer {

    //创建一个类的实例方便调用
    public static final Anemo ANEMO = (Anemo) Element.create(Element.ElementType.ANEMO,0);
    public static final Cryo CRYO = (Cryo) Element.create(Element.ElementType.CRYO,0);
    public static final Dendro DENDRO = (Dendro) Element.create(Element.ElementType.DENDRO,0);
    public static final Electro ELECTRO = (Electro) Element.create(Element.ElementType.ELECTRO,0);
    public static final Frozen FROZEN = (Frozen) Element.create(Element.ElementType.FROZEN,0);
    public static final Geo GEO = (Geo) Element.create(Element.ElementType.GEO,0);
    public static final Hydro HYDRO = (Hydro) Element.create(Element.ElementType.HYDRO,0);
    public static final Pyro PYRO = (Pyro) Element.create(Element.ElementType.PYRO,0);

    //一些成员
    private final HashSet<Element> elements = new HashSet<>();//该映射用于存储该容器所拥有的元素实例
    private final LivingEntity owner;//此为该容器拥有者
    private LivingEntity latestAttacker;//最后施加元素的生物


    //!Debug!
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean debugMode = true;//此处打开debug模式，等到正式发布时删除（将此处变量设为false）这部分代码
    private int displayRateCD = 0;

    //正常生存可以调整的模式
    public boolean displayElement = false;//是否显示元素（这里应该是是否显示图标，与上方debug模式作区分）

    //owner的各类属性
    private double mastery;//元素精通
    private double resistance;//抗性
    private double bonus;//增伤

    //CD类别成员
    private int electroChargedCD = 0;//感电反应计时器
    private int combustionCD = 0;//燃烧反应计时器
    private int superConductCD = 0;//超导反应计时器

    //owner的状态
    private boolean isCombustion = false;
    private boolean isElectroCharged = false;
    private boolean isSuperConduct = false;

    //辅助数据结构
    private HashMap<Class<? extends Element>,Boolean> hasElement;//此处使用懒加载节省空间
    private final Vector<Element> deadElements = new Vector<>();//这是元素量小于0的元素的集合

    public ElementContainer(LivingEntity owner){
        this.owner = owner;
    }

    public void tick(){
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
                    stringBuilder.append(element.toString()).append(" ").append(gauge);
                }
                else {
                    stringBuilder.append(",").append(element.toString()).append(" ").append(gauge);
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

        //将其中元素的状态更新
        for(Element element : elements){
            hasElement.put(element.getClass(),true);
        }

        /*
        超导反应相关逻辑实现
         */
        if(isSuperConduct)


        /*
        感电&燃烧反应实现：
            检查容器中是否有超过两种元素，如果有，检查元素类别
         */
        if(elements.size() == 2){
            if(has(Pyro.class) && has(Dendro.class)){//燃烧
                if(debugMode){
                    System.out.println("Is Com");
                }

                //将isCombustion标记为true
                isCombustion = true;

                //这里实现CD的自然减少
                if(combustionCD > 0){
                    combustionCD--;
                }
                else{
                    combustionCD = 5;//重置cd

                    if(!owner.isOnFire()){
                        /*
                        此处当拥有者没有在燃烧时，赋予一个持续一秒的燃烧状态（此处是原版的燃烧，与燃烧反应区分）
                         */
                        owner.setOnFireFor(5);
                    }
                }


                /*
                    下面实现对于元素量的操作：
                        火元素设为两单位，草元素每秒减少0.4（0.02gpt）
                     */
                for(Element element : elements){
                    if(element instanceof Pyro){
                        element.setGauge(2);
                    }
                    else if(element instanceof Dendro){
                        element.subGauge(0.02);
                    }
                }
            }
            else{//如果并非火草共存
                isCombustion = false;
            }
            if (has(Hydro.class) && has(Electro.class)) {//感电
                if(electroChargedCD > 0){
                    electroChargedCD--;
                }
                else{
                    electroChargedCD = 20;
                    owner.damage(getDamageSource(), 0.6f);
                }
            }
            else {//如果没有水雷共存
                isElectroCharged = false;
            }
        }
    }

    /*
        下面处理产生元素附着的情况
     */
    public void applyElement(Element element, @Nullable LivingEntity source){
        element.setOwner(owner);
        element.setAttacker(source);

        //更新容器的最后攻击者(反应触发者）
        latestAttacker = element.getAttacker();

        //获取攻击者的元素精通和元素反应容器
        ElementContainer newElementContainer;
        double attackerMastery;
        if(element.getOwner() instanceof LivingEntityHolder livingEntityHolder){
            newElementContainer = livingEntityHolder.elementWorld$getElementContainer();
            attackerMastery = newElementContainer.getMastery();
        }
        else{
            newElementContainer = null;
            attackerMastery = 0;
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
                } else if (bRElement instanceof Cryo) {//冰！
                    //此处生成冻元素
                    elements.add(new Frozen(Math.min(gauge, beGauge) * 2));
                    bRElement.subGauge(gauge);
                }
                else if(bRElement instanceof Frozen){//冻
                    addElement(element);
                }
                else if (bRElement instanceof Element) {//雷
                    addElement(element);
                    /*
                    感电反应相关逻辑写在tick()
                     */
                    electroChargedCD = 20;
                }
                else if (bRElement instanceof Dendro) {//草
                    /*
                    这里缺少草反应相关逻辑（还没想好草反应怎么写）
                     */
                    bRElement.subGauge(gauge);
                }
                else {
                    System.out.println("[Warning] ElementApplied Error!");
                }
            }

            //如若添加的元素为火
            if (element instanceof Pyro) {
                if (bRElement instanceof Hydro) {//水
                    bRElement.subGauge(gauge * 0.5);
                }
                else if (bRElement instanceof Electro) {//雷
                    removeElement(bRElement);
                /*
                下面实现超载：
                超载反应特征：爆炸并且造成伤害
                    先获取容器拥有者的坐标位置和服务器世界实例，
                    然后在坐标位置生成一个有伤害的爆炸
                 */
                    if (newElementContainer != null) {
                        overload(newElementContainer);
                    }
                }
                else if(bRElement instanceof Cryo){//冰！！
                    bRElement.subGauge(gauge*2);
                }
                else if(bRElement instanceof Frozen){
                    bRElement.subGauge(gauge*2);
                }
                else if(bRElement instanceof Dendro){//草
                    addElement(element);
                    /*
                    燃烧反应的逻辑在tick()
                     */
                    combustionCD = 5;
                }
                else{
                    System.out.println("[Warning] ElementApplied Error!");
                }
            }
            //如果添加的元素为雷
            if(element instanceof Electro){
                if(bRElement instanceof Pyro){//火
                    removeElement(bRElement);
                    if (newElementContainer != null) {
                        overload(newElementContainer);
                    }
                }
                else if(bRElement instanceof Hydro){
                    addElement(element);
                }
                else if(bRElement instanceof Cryo){
                    isSuperConduct = true;
                    superConductCD = 240;
                }
            }
        }
    }



    /*
    private方法
     */
    //这个是带有损耗的元素附着模式
    private void addElement(Element element){
        element.setGauge(element.getGauge()*0.8);//元素附着损耗
        elements.add(element);
    }

    //移除对应元素
    private void removeElement(Element element){
        elements.remove(element);

        //懒加载这个映射
        if(hasElement == null){hasElement = new HashMap<>();}
        hasElement.put(element.getClass(),false);
    }

    /*
    下面实现超载：
     超载反应特征：爆炸并且造成伤害
        先获取容器拥有者的坐标位置和服务器世界实例，
      然后在坐标位置生成一个有伤害的爆炸
    */
    private void overload(LivingEntity source){
        ElementContainer container;
        if(source instanceof LivingEntityHolder holder){
            container = holder.elementWorld$getElementContainer();
        }
        else {
            container = null;
        }

        Vec3d playerPos = owner.getPos();
        Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                .createExplosion(source, playerPos.x, playerPos.y + owner.getHeight()/2, playerPos.z, 1, World.ExplosionSourceType.NONE);
        if (container != null) {
            owner.damage(getDamageSource(),damage(3,container));
        }
        else{
            System.out.println("[Warning] 该实体没有正确加载Mixin");
        }
    }

    private void overload(ElementContainer attackerElementContainer){
        Vec3d playerPos = owner.getPos();
        Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                .createExplosion(attackerElementContainer.getOwner(), playerPos.x, playerPos.y + owner.getHeight()/2, playerPos.z, 1, World.ExplosionSourceType.NONE);
        owner.damage(getDamageSource(), damage(3, attackerElementContainer));
    }

    /*
    计算最终结算伤害
     */
    public float damage(float originValue,ElementContainer attackerElementContainer){
        return (float) (originValue//基础数值
                *(1+attackerElementContainer.getMastery())//精通乘区
                *(1-resistance)//抗性乘区
                *(1+attackerElementContainer.getBonus()));//增伤乘区
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



    /*
    以下为getter&setter
     */

    public boolean isCombustion(){
        return isCombustion;
    }

    public boolean isElectroCharged(){
        return isElectroCharged;
    }

    public boolean isSuperConduct() {
        return isSuperConduct;
    }

    public Collection<Element> getElements(){
        return elements;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public double getMastery(){
        return mastery;
    }

    public double getBonus(){
        return bonus;
    }

    public double getResistance(){
        return resistance;
    }

    /*
    该方法创建的伤害类型为持续反应伤害，此处source是玩家本身，attacker是最后施加元素的生物
     */
    private DamageSource getDamageSource(){
        Registry<DamageType> damageTypeRegistry = owner.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        DamageType type = damageTypeRegistry.get(ElementWorld.ELEMENT_DAMAGE);
        return new DamageSource(damageTypeRegistry.getEntry(type),owner,latestAttacker);
    }


}
