package com.elementworld.command.argumenttypes;

import com.elementworld.elements.Element;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ElementTypeArgumentType implements ArgumentType<String> {
    private static final DynamicCommandExceptionType INVALID_ELEMENT = new DynamicCommandExceptionType(
            value -> Text.literal("Unknown element type: " + value)
    );

    public static ElementTypeArgumentType element() {
        return new ElementTypeArgumentType();
    }

    public static Element.ElementType getElement(CommandContext<ServerCommandSource> context, String name) {
        return Element.ElementType.valueOf(context.getArgument(name, String.class));
    }

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        String input = stringReader.readUnquotedString();
        String normalizedInput = input.toUpperCase(Locale.ROOT);
        try {
            return Element.ElementType.valueOf(normalizedInput).name();
        } catch (IllegalArgumentException exception) {
            throw INVALID_ELEMENT.create(input);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (Element.ElementType type : Element.ElementType.values()) {
            String suggestion = type.name().toLowerCase(Locale.ROOT);
            if (suggestion.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }
}
