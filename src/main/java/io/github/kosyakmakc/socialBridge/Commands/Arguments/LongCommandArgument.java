package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class LongCommandArgument extends CommandArgument<Long> implements ICommandArgumentNumeric<Long> {
    private final String name;
    private final Long minimum;
    private final Long maximum;

    public LongCommandArgument(String name, Long minimum, Long maximum) {
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public CommandArgumentDataType getDataType() {
        return CommandArgumentDataType.Long;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Long getValue(StringReader args) throws ArgumentFormatException {
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
            var value = Long.parseLong(wordWriter.toString());
            if (value < minimum && value > maximum) {
                throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_MIN_MAX_ERROR);
            }
            return value;

        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(MessageKey.INVALID_ARGUMENT_NOT_A_LONG);
        }
    }

    @Override
    public Long getMin() {
        return minimum;
    }

    @Override
    public Long getMax() {
        return maximum;
    }
}
