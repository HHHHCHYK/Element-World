package com.elementworld.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class Hello {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 定义命令 "/hello"
        LiteralArgumentBuilder<ServerCommandSource> helloCommand = CommandManager.literal("hello")
                .executes(context -> {
                    // 当命令不带参数时执行的逻辑
                    context.getSource().sendMessage(Text.literal("Hello, world!"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            // 当命令带参数时执行的逻辑
                            String name = StringArgumentType.getString(context, "name");
                            context.getSource().sendMessage(Text.literal("Hello, " + name + "!"));
                            return Command.SINGLE_SUCCESS;
                        })
                );

        // 注册命令
        dispatcher.register(helloCommand);
    }
}
