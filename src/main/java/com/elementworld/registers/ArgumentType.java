package com.elementworld.registers;

import com.elementworld.ElementWorld;
import com.elementworld.command.ArgumentTypes.ElementTypeArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ArgumentType {
    public static void register(){
        ArgumentTypeRegistry.registerArgumentType(
                new Identifier(ElementWorld.MOD_ID,"element_types"),//唯一标识符
                ElementTypeArgumentType.class,//被注册类
                ConstantArgumentSerializer.of(ElementTypeArgumentType::element)//序列器
        );
    }


}
