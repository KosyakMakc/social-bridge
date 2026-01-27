package io.github.kosyakmakc.socialBridge.paper;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.ICommandArgumentSuggestions;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class BridgeCommandSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final ICommandArgumentSuggestions argument;

    public BridgeCommandSuggestionProvider(ICommandArgumentSuggestions argument) {
        this.argument = argument;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {

        return argument.getSuggestions()
            .thenCompose(suggestions -> {
                for (var suggest : suggestions) {
                    builder.suggest(suggest);
                }

                return builder.buildFuture();
            });
    }

}
