package com.elementworld.command;

import com.elementworld.ElementContainer;
import com.elementworld.command.ArgumentTypes.ElementTypeArgumentType;
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

    //最终注册操作
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(elementCommandBuild());
    }

    /*
        这个命令(Command)用于对实体的元素附着进行操作
    */
    private static LiteralArgumentBuilder<ServerCommandSource> elementCommandBuild(){
        return CommandManager.literal("element")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.literal("list")
                        .then(CommandManager.argument("target",EntityArgumentType.entity())
                                .executes(commandContext -> {
                                    if(EntityArgumentType.getEntity(commandContext, "target") instanceof LivingEntity entity){
                                        if (!(entity instanceof LivingEntityHolder)){
                                            commandContext.getSource().sendMessage(Text.literal("The Mixin error!"));
                                        }
                                        else {
                                            ElementContainer container = ((LivingEntityHolder) entity).getElementContainer$EW();
                                            if(container.isEmpty()){
                                                commandContext.getSource().sendMessage(Text.literal("The Container is empty!"));
                                            }
                                            else {
                                                for(Element element : container.getElements()){
                                                    commandContext.getSource().sendMessage(Text.literal(element.toString()));
                                                }
                                            }
                                        }
                                    }else {
                                        commandContext.getSource().sendMessage(Text.literal("Wrong target!"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("target",EntityArgumentType.entity())
                                .then(CommandManager.argument("ElementType", ElementTypeArgumentType.element())
                                        .then(CommandManager.argument("gauge", DoubleArgumentType.doubleArg())
                                                .executes(commandContext -> {
                                                    ServerCommandSource scs = commandContext.getSource();
                                                    Entity entity = EntityArgumentType.getEntity(commandContext,"target");
                                                    if(!(entity instanceof LivingEntity)){
                                                        scs.sendMessage(Text.literal("Wrong target!"));
                                                    }
                                                    else if(!(entity instanceof LivingEntityHolder)){
                                                        scs.sendMessage(Text.literal("Mixin Error!"));
                                                    }
                                                    else{
                                                        Element.ElementType element = ElementTypeArgumentType.getElement(commandContext,"ElementType");
                                                        ElementContainer container = ((LivingEntityHolder) entity).getElementContainer$EW();

                                                        /*
                                                        container.applyElement(
                                                                Objects.requireNonNull(
                                                                        Element.create(element,
                                                                                DoubleArgumentType.getDouble(commandContext, "gauge"))),
                                                                null);
                                                         */
                                                        if(entity instanceof LivingEntity livingEntity){
                                                            DamageSource damageSource = DamageSourceHolder.createDamageSource(livingEntity.getWorld(),
                                                                    livingEntity, null);

                                                            ((DamageSourceHolder)damageSource).getEWDamageSource$EW().setElement(Element.create(
                                                                    element,
                                                                    DoubleArgumentType.getDouble(commandContext, "gauge")));
                                                            livingEntity.damage(damageSource, 2);
                                                        }
                                                        scs.sendMessage(Text.literal("成功添加"+ element));

                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })))));
    }

}
