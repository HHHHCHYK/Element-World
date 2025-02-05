package com.elementworld.command.ArgumentTypes;

import com.elementworld.elements.Element;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import jdk.jshell.SourceCodeAnalysis;

import java.util.concurrent.CompletableFuture;

public class ElementTypeArgumentType implements ArgumentType<Element.Elements> {

    /*
    创建单例
     */
    public static ElementTypeArgumentType element(){
        return new ElementTypeArgumentType();
    }

    /*
    判断是否匹配
     */
    @Override
    public Element.Elements parse(StringReader stringReader) throws CommandSyntaxException {
        String input = stringReader.readUnquotedString();
        return Element.Elements.valueOf(input.toUpperCase());
    }

    /*
    自动补全建议
     */
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
        for(Element.Elements type : Element.Elements.values()){
            if(type.name().toLowerCase().startsWith(builder.getRemainingLowerCase())){
                builder.suggest(type.name().toLowerCase());
            }
        }
        return builder.buildFuture();
    }
}
