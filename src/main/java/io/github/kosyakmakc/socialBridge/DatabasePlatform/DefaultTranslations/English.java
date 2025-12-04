package io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.util.List;

public class English implements ITranslationSource {
    @Override
    public String getLanguage() {
        return LocalizationService.defaultLocale;
    }

    @Override
    public List<LocalizationRecord> getRecords() {
        return List.of(
                new LocalizationRecord(MessageKey.INTERNAL_SERVER_ERROR.key(), "<red>Error has occurred on server side.</red>"),

                new LocalizationRecord(MessageKey.INVALID_ARGUMENT.key(), "Argument is invalid."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_ARE_EMPTY.key(), "Argument is required, but got empty."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_BOOLEAN.key(), "Argument is not a boolean."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_INTEGER.key(), "Argument is not a 32-bit integer."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_LONG.key(), "Argument is not a 64-bit integer."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_FLOAT.key(), "Argument is not a 32-bit float number."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_DOUBLE.key(), "Argument is not a 64-bit float number(double).")
        );
    }
}
