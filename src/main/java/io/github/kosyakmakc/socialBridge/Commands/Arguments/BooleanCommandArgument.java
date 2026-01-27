package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;

class BooleanCommandArgument extends CommandArgument<Boolean> implements ICommandArgumentSuggestions {
    private final String name;

    public BooleanCommandArgument(String name) {
        this.name = name;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.Boolean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<String[]> getSuggestions() {
        return CompletableFuture.completedFuture(new String[] { "true", "false" });
    }

    @Override
    public Boolean getValue(StringReader args) throws ArgumentFormatException {
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

        try {
            return Boolean.parseBoolean(wordWriter.toString().toLowerCase());
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_BOOLEAN);
        }
    }
}
