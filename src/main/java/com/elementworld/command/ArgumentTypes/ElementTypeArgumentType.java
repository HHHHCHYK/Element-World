package com.elementworld.command.ArgumentTypes;

import com.elementworld.elements.Element;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class ElementTypeArgumentType implements ArgumentType<String> {

    /*
    创建单例
     */
    public static ElementTypeArgumentType element(){
        return new ElementTypeArgumentType();
    }

    public static Element.ElementType getElement(CommandContext<ServerCommandSource> context, String name){
        return Element.ElementType.valueOf(context.getArgument(name,String.class));
    }

    /*
    判断是否匹配
        parse：是否符合语法
     */
    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        String input = stringReader.readUnquotedString();
        return Element.ElementType.valueOf(input.toUpperCase()).toString();
    }

    /*
    自动补全建议
     */
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
        for(Element.ElementType type : Element.ElementType.values()){
            if(type.name().toLowerCase().startsWith(builder.getRemainingLowerCase())){
                builder.suggest(type.name().toLowerCase());
            }
        }
        return builder.buildFuture();
    }
}
