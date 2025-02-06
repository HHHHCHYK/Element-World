package com.elementworld;

import com.elementworld.elements.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElementContainer {
    private final ArrayList<Element> elements = new ArrayList<Element>();
    private final LivingEntity owner;

    public boolean displayElement = false;
    private int update = 0;

    public ElementContainer(LivingEntity owner){

        this.owner = owner;
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
            addElement(element);
        }
        /*
        如果只有一个元素被附着
         */
        else if (elements.size() == 1) {
            Element beReactionElement = elements.iterator().next();

            double gauge = element.getGauge();//后手元素元素量
            double beGauge = beReactionElement.getGauge();//附着元素元素量

            if (beReactionElement.getClass() == element.getClass()) {
                beReactionElement.setGauge(Math.max(gauge, beGauge));
            }

            //如若被添加元素为水元素
            if (element instanceof Hydro) {


                if (beReactionElement instanceof Pyro) {//火
                    beReactionElement.subGauge(gauge * 2);
                } else if (beReactionElement instanceof Cryo) {//冰！
                    elements.add(new Frozen(Math.min(gauge, beGauge) * 2));
                    beReactionElement.subGauge(gauge);
                } else if (beReactionElement instanceof Element) {//雷
                    addElement(element);
                } else if (beReactionElement instanceof Dendro) {
                    beReactionElement.subGauge(gauge);
                } else {
                    System.out.println("[Warning] Wrong element be applied!");
                }
            }

            //如若添加的元素为火
            if (element instanceof Pyro) {
                if (beReactionElement instanceof Hydro) {
                    beReactionElement.subGauge(gauge * 0.5);
                } else if (beReactionElement instanceof Electro) {
                    beReactionElement.subGauge(gauge);

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
            }
        }
    }

    public void tick(){

        for (Element element : elements) {
            /*
            下面运行每个附着元素的tick方法
             */
            element.tick();
            /*
            移除元素量小于等于零的元素
             */
            if(element.getGauge()<=0){
                removeElement(element);
            }

            if(displayElement && update == 0){
                update =(update+1)%10;
                owner.sendMessage(Text.literal("Debug!Has element:" + element));
            }
        }
    }


    /*
    private方法
     */
    private void addElement(Element element){
        element.setGauge(element.getGauge()*0.8);//元素附着损耗
        elements.add(element);
    }

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
