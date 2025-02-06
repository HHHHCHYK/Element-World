package com.elementworld;

import com.elementworld.elements.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElementContainer {
    private final HashSet<Element> elements = new HashSet<Element>();
    private final LivingEntity owner;

    //!Debug!
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean debugMode = true;

    //正常生存可以调整的模式
    public boolean displayElement = false;

    //owner的各类属性
    public int mastery;

    //CD类别成员
    private int electroChargedCD = 0;
    private int combustionCD = 0;

    public ElementContainer(LivingEntity owner){
        this.owner = owner;
    }

    public void tick(){

        //使得overloadCD流动
        if(electroChargedCD >0){
            electroChargedCD--;
        }

        Vector<Element> deadElements = new Vector<Element>();
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

        //Debug模式：显示玩家目前所拥有的元素
        if(owner instanceof PlayerEntity player && debugMode){
            boolean flag = true;
            for(Element element : elements){
                String gauge = String.format("%.2f",element.getGauge());
                if(flag){
                    flag = false;
                    player.sendMessage(Text.literal(element.toString() + gauge),true);
                    continue;
                }
                else {
                    player.sendMessage(Text.literal(" , " + element.toString() + gauge),true);
                }
            }
        }

        /*
        感电&超载反应实现：
            检查容器中是否有超过两种元素，如果有，检查元素类别
         */

    }

    /*
        下面处理产生元素附着的情况
     */
    public void applyElement(Element element, @Nullable LivingEntity source){
        element.setOwner(owner);
        element.setSource(source);


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
                } else if (bRElement instanceof Electro) {//雷
                    bRElement.subGauge(gauge);
                /*
                下面实现超载：
                超载反应特征：爆炸并且造成伤害
                    先获取容器拥有者的坐标位置和服务器世界实例，
                    然后在坐标位置生成一个有伤害的爆炸
                 */
                    Vec3d playerPos = owner.getPos();
                    Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                            .createExplosion(owner, playerPos.x, playerPos.y, playerPos.z, 3, World.ExplosionSourceType.BLOCK);
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
                }
                else{
                    System.out.println("[Warning] ElementApplied Error!");
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
    }

    /*
    public方法
     */
    public boolean isEmpty(){
        return elements.isEmpty();
    }


    /*
    以下为getter&setter
     */

    public Collection<Element> getElements(){
        return elements;
    }

    public LivingEntity getOwner() {
        return owner;
    }
}
