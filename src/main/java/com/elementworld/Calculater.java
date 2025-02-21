package com.elementworld;

import net.minecraft.util.math.Vec3d;

public class Calculater {
    public static double distance(Vec3d vec1,Vec3d vec2){
        return Math.sqrt(Math.pow(vec1.x-vec2.x,2) + Math.pow(vec1.y - vec2.y,2) + Math.pow(vec1.z - vec2.z,2));
    }
}
