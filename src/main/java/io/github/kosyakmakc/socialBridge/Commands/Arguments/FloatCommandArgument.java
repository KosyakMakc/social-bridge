package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class FloatCommandArgument extends CommandArgument<Float> implements ICommandArgumentNumeric<Float> {
    private final String name;
    private final Float minimum;
    private final Float maximum;

    public FloatCommandArgument(String name, Float minimum, Float maximum) {
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
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
            var value = Float.parseFloat(wordWriter.toString());
            if (value < minimum && value > maximum) {
                throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_MIN_MAX_ERROR);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_FLOAT);
        }
    }

    @Override
    public Float getMin() {
        return minimum;
    }

    @Override
    public Float getMax() {
        return maximum;
    }
}
