package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class FloatCommandArgument extends CommandArgument<Float> {
    private final String name;

    public FloatCommandArgument(String name) {
        this.name = name;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.Float;
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
    public Float getValue(StringReader args) throws ArgumentFormatException {
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
            return Float.parseFloat(wordWriter.toString());
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_FLOAT);
        }
    }
}
