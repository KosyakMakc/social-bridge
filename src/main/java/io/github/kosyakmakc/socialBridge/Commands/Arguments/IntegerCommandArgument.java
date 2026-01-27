package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class IntegerCommandArgument extends CommandArgument<Integer> implements ICommandArgumentNumeric<Integer> {
    private final String name;
    private final int minimum;
    private final int maximum;

    public IntegerCommandArgument(String name, int minimum, int maximum) {
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.Integer;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Integer getValue(StringReader args) throws ArgumentFormatException {
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
            var value = (int) Double.parseDouble(wordWriter.toString());
            if (value < minimum && value > maximum) {
                throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_MIN_MAX_ERROR);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_INTEGER);
        }
    }

    @Override
    public Integer getMin() {
        return minimum;
    }

    @Override
    public Integer getMax() {
        return maximum;
    }
}
