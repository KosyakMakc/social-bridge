package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class StringCommandArgument extends CommandArgument<String> implements ICommandArgumentSuggestions {
    private static final char quoteChar = '"';
    private static final char escapeChar = '\\';
    private final String name;
    private final Supplier<CompletableFuture<String[]>> suggestionProvider;

    public StringCommandArgument(String name, Supplier<CompletableFuture<String[]>> suggestionProvider) {
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
        var prevCharCode = -1;

        try {
            var firstSymbol = (char) args.read();
            var isSimpleMode = firstSymbol != quoteChar;
            if (isSimpleMode) {
                wordWriter.write(firstSymbol);
            }

            while ((charCode = args.read()) != -1) {
                var symbol = (char) charCode;

                if (isSimpleMode && Character.isWhitespace(symbol)) {
                    break;
                }

                if (!isSimpleMode && symbol == quoteChar) {
                    var prevSymbol = (char) prevCharCode;
                    if (prevSymbol != escapeChar) {
                        var nextSymbolCode = args.read();
                        if (nextSymbolCode == -1) {
                            break;
                        }

                        if (Character.isWhitespace((char) nextSymbolCode)) {
                            break;
                        }

                        wordWriter.write(symbol);
                        wordWriter.write(nextSymbolCode);
                        continue;
                    }
                }

                wordWriter.write(symbol);
                prevCharCode = charCode;
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
