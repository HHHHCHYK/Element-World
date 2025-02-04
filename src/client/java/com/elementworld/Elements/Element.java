package com.elementworld.Elements;


public class Element {
    public enum Gauge{
        WEEK,STRENGTH,ULTRA
    }

    private int ElementGauge;

    public Element(Gauge gauge){
        switch (gauge){
            case WEEK -> ElementGauge = 1;
            case STRENGTH -> ElementGauge = 2;
            case ULTRA -> ElementGauge =4;
        }
    }

    public void tick(){

    }
}
