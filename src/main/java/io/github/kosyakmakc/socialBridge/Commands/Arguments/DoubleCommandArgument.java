package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class DoubleCommandArgument extends CommandArgument<Double> implements ICommandArgumentNumeric<Double> {
    private final String name;
    private final Double minimum;
    private final Double maximum;

    public DoubleCommandArgument(String name, Double minimum, Double maximum) {
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.Double;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Double getValue(StringReader args) throws ArgumentFormatException {
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
            var value = Double.parseDouble(wordWriter.toString());
            if (value < minimum && value > maximum) {
                throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_MIN_MAX_ERROR);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_DOUBLE);
        }
    }

    @Override
    public Double getMin() {
        return minimum;
    }

    @Override
    public Double getMax() {
        return maximum;
    }
}
