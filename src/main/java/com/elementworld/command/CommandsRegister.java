package com.elementworld.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.x;

public class CommandsRegister {
    CommandDispatcher<ServerCommandSource> dispatcher;

    //最终注册操作
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(elementCommandBuild());
    }

    /*
        这个命令用于对实体的元素附着进行操作
    */
    private static LiteralArgumentBuilder<ServerCommandSource> elementCommandBuild(){
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> addBuild = CommandManager
                .argument("target", EntityArgumentType.entity())
                .then(CommandManager.argument("ElementType",StringArgumentType.word()));
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return (LiteralArgumentBuilder<ServerCommandSource>) CommandManager.literal("element")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.argument("method", StringArgumentType.word())
                        .executes(commandContext -> {
                            String string = StringArgumentType.getString(commandContext,"method");
                            if(string.equals("add")){

                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

}
