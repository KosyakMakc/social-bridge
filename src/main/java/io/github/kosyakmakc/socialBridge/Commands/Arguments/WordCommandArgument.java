package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class WordCommandArgument extends CommandArgument<String> implements ICommandArgumentSuggestions {
    private final String name;
    private final Supplier<CompletableFuture<String[]>> suggestionProvider;

    public WordCommandArgument(String name, Supplier<CompletableFuture<String[]>> suggestionProvider) {
        this.name = name;
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.String;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<String[]> getSuggestions() {
        return suggestionProvider.get();
    }

    @Override
    public String getValue(StringReader args) throws ArgumentFormatException {
        var wordWriter = new StringWriter();
        int charCode;

        try {
            while ((charCode = args.read()) != -1) {
                var symbol = (char) charCode;

                if (Character.isWhitespace(symbol)) {
                    break;
                }

                wordWriter.write(symbol);
            }
        } catch (IOException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT);
        }

        var word = wordWriter.toString();

        if (word.isBlank()) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_ARE_EMPTY);
        }

        return word;
    }
}
