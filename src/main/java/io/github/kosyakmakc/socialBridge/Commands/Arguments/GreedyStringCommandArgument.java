package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class GreedyStringCommandArgument extends CommandArgument<String> implements ICommandArgumentString {
    private final String name;
    private final Supplier<CompletableFuture<String[]>> suggestionProvider;

    public GreedyStringCommandArgument(String name, Supplier<CompletableFuture<String[]>> suggestionProvider) {
        this.name = name;
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.GreedyString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<String[]> getAutoCompletes() {
        return suggestionProvider.get();
    }

    @Override
    public String getValue(StringReader args) throws ArgumentFormatException {
        var greedyStringWriter = new StringWriter();

        try {
            args.transferTo(greedyStringWriter);
        } catch (IOException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT);
        }

        return greedyStringWriter.toString();
    }
}
