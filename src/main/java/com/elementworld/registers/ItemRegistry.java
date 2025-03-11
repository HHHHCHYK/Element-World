package com.elementworld.registers;

import com.elementworld.ElementWorld;
import com.item.GuiStick;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {
    public static final GuiStick GUI_STICK = new GuiStick(new Item.Settings().maxCount(1));
    public static void registerItems() {
        // Register items here
        Registry.register(Registries.ITEM,new Identifier(ElementWorld.MOD_ID,"gui_stick"), GUI_STICK);//注册GuiStick
    }
}
