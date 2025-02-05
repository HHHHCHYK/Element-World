package com.elementworld;

import com.elementworld.elements.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ElementContainer {
    private Collection<Element> elements;
    private final LivingEntity owner;

    public ElementContainer(LivingEntity owner){

        this.owner = owner;
    }

    /*
        下面处理产生元素附着的情况
     */
    public void applicationElement(Element element,LivingEntity source){

        if(elements.isEmpty()){//集合为空
            addElement(element);

        } else if (elements.size() == 1) {//单元素附着
            Element beReactionElement = elements.iterator().next();

            double gauge = element.getGauge();//后手元素元素量
            double beGauge = beReactionElement.getGauge();//附着元素元素量

            if(beReactionElement.getClass() == element.getClass()){
                beReactionElement.setGauge(Math.max(gauge,beGauge));
            }

            //如若被添加元素为水元素
            if(element instanceof Hydro){


                if (beReactionElement instanceof Pyro){//火
                    beReactionElement.subGauge(gauge*2);
                }
                else if(beReactionElement instanceof Cryo){//冰！
                    elements.add(new Frozen(Math.min(gauge, beGauge)*2,owner));
                    beReactionElement.subGauge(gauge);
                }
                else if(beReactionElement instanceof Element){//雷
                    addElement(element);
                }
                else if(beReactionElement instanceof Dendro){
                    beReactionElement.subGauge(gauge);
                }
                else {
                    System.out.println("[Warning] Wrong element be applied!");
                }
            }

            //如若添加的元素为火
            if(element instanceof Pyro){
                if(beReactionElement instanceof Hydro){
                    beReactionElement.subGauge(gauge*0.5);
                }
                else if(beReactionElement instanceof Electro){
                    beReactionElement.subGauge(gauge);

                    /*
                    下面实现超载：
                    超载反应特征：爆炸并且造成伤害
                        先获取容器拥有者的坐标位置和服务器世界实例，
                        然后在坐标位置生成一个有伤害的爆炸
                     */
                    Vec3d playerPos = owner.getPos();
                    Objects.requireNonNull(Objects.requireNonNull(owner.getServer()).getWorld(owner.getWorld().getRegistryKey()))
                            .createExplosion(owner, playerPos.x,playerPos.y,playerPos.z,3, World.ExplosionSourceType.BLOCK);
                }
            }
        }
    }

    public void tick(){
        /*
        下面运行每个附着元素的tick方法
         */
        for(Element element : elements){
            element.tick();
        }

    }




    private void addElement(Element element){
        element.setGauge(element.getGauge()*0.8);//元素附着损耗
        elements.add(element);
    }

    public LivingEntity getOwner() {
        return owner;
    }
}
