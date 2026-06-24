package com.elementworld;

import net.minecraft.util.math.Vec3d;

public final class Calculator {
    private Calculator() {
    }

    public static double distance(Vec3d first, Vec3d second) {
        return first.distanceTo(second);
    }
}
