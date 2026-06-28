package com.elementworld;

import com.elementworld.command.Commands;
import com.elementworld.registers.ItemRegistry;
import com.elementworld.registers.ModArgumentTypes;
import com.elementworld.registers.ModEnchantments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementWorld implements ModInitializer {
    public static final String MOD_ID = "elementworld";
    public static final Identifier ELEMENT_DAMAGE_ID = new Identifier(MOD_ID, "element_damage");
    public static final Identifier ELEMENT_TYPES_PACKET_ID = new Identifier(MOD_ID, "element_types");
    public static final Identifier FLOATING_TEXT_PACKET_ID = new Identifier(MOD_ID, "floating_text");
    public static final RegistryKey<DamageType> ELEMENT_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ELEMENT_DAMAGE_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean DEBUG = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ElementWorld");
        ItemRegistry.registerItems();
        ModEnchantments.register();
        ModArgumentTypes.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.register(dispatcher));
    }
}
