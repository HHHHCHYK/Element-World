package com.elementworld.registers;

import com.elementworld.ElementWorld;
import com.elementworld.command.argumenttypes.ElementTypeArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class ModArgumentTypes {
    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(
                ElementWorld.ELEMENT_TYPES_PACKET_ID,
                ElementTypeArgumentType.class,
                ConstantArgumentSerializer.of(ElementTypeArgumentType::element)
        );
    }
}
