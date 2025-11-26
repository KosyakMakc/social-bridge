package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class WordCommandArgument extends CommandArgument<String> {
    private final String name;

    public WordCommandArgument(String name) {
        this.name = name;
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
    public String[] getAutoCompletes() {
        return new String[0];
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
