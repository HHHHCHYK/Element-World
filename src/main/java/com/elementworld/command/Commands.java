package com.elementworld.command;

import com.elementworld.command.argumenttypes.ElementTypeArgumentType;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.DamageSourceHolder;
import com.elementworld.interfaces.LivingEntityHolder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class Commands {
    private static final String TARGET_ARGUMENT = "target";
    private static final String ELEMENT_TYPE_ARGUMENT = "element_type";
    private static final String GAUGE_ARGUMENT = "gauge";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(elementCommandBuild());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> elementCommandBuild() {
        return CommandManager.literal("element")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(listCommand())
                .then(addCommand());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> listCommand() {
        return CommandManager.literal("list")
                .then(CommandManager.argument(TARGET_ARGUMENT, EntityArgumentType.entity())
                        .executes(commandContext -> {
                            Entity target = EntityArgumentType.getEntity(commandContext, TARGET_ARGUMENT);
                            if (!(target instanceof LivingEntity livingEntity)) {
                                commandContext.getSource().sendMessage(Text.translatable("commands.elementworld.target.invalid"));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!(livingEntity instanceof LivingEntityHolder holder)) {
                                commandContext.getSource().sendMessage(Text.translatable("commands.elementworld.mixin.missing"));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (holder.getElementContainer$EW().isEmpty()) {
                                commandContext.getSource().sendMessage(Text.translatable("commands.elementworld.container.empty"));
                                return Command.SINGLE_SUCCESS;
                            }

                            for (Element element : holder.getElementContainer$EW().getElements()) {
                                commandContext.getSource().sendMessage(Text.literal(element.toString()));
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> addCommand() {
        return CommandManager.literal("add")
                .then(CommandManager.argument(TARGET_ARGUMENT, EntityArgumentType.entity())
                        .then(CommandManager.argument(ELEMENT_TYPE_ARGUMENT, ElementTypeArgumentType.element())
                                .then(CommandManager.argument(GAUGE_ARGUMENT, DoubleArgumentType.doubleArg())
                                        .executes(commandContext -> {
                                            ServerCommandSource source = commandContext.getSource();
                                            Entity target = EntityArgumentType.getEntity(commandContext, TARGET_ARGUMENT);
                                            if (!(target instanceof LivingEntity livingEntity)) {
                                                source.sendMessage(Text.translatable("commands.elementworld.target.invalid"));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            if (!(livingEntity instanceof LivingEntityHolder)) {
                                                source.sendMessage(Text.translatable("commands.elementworld.mixin.missing"));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Element.ElementType elementType = ElementTypeArgumentType.getElement(commandContext, ELEMENT_TYPE_ARGUMENT);
                                            DamageSource damageSource = DamageSourceHolder.createDamageSource(livingEntity.getWorld(), livingEntity, null);
                                            ((DamageSourceHolder) damageSource).getEWDamageSource$EW().setElement(Element.create(
                                                    elementType,
                                                    DoubleArgumentType.getDouble(commandContext, GAUGE_ARGUMENT)
                                            ));
                                            livingEntity.damage(damageSource, 0.1f);
                                            source.sendMessage(Text.translatable(
                                                    "commands.elementworld.add.success",
                                                    elementType.name().toLowerCase()
                                            ));
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }
}
