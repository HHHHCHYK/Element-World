package com.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GuiStick extends Item{

    public GuiStick(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient && user instanceof ServerPlayerEntity serverPlayer){
            user.sendMessage(Text.literal("Be used"),false);
            //打开gui
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
