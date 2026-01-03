package io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations;

import java.util.List;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

public class Russian implements ITranslationSource {
    @Override
    public String getLanguage() {
        return LocalizationService.defaultLocale;
    }

    @Override
    public List<LocalizationRecord> getRecords() {
        return List.of(
                new LocalizationRecord(MessageKey.INTERNAL_SERVER_ERROR.key(), "<red>Внутренная ошибка на серверной стороне.</red>"),

                new LocalizationRecord(MessageKey.INVALID_ARGUMENT.key(), "Аргумент некорректный."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_ARE_EMPTY.key(), "Аргумент обязателен, но получена пустая строка."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_BOOLEAN.key(), "Аргумент не логического типа(да/нет)."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_INTEGER.key(), "Аргумент не 32-битное целое число."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_LONG.key(), "Аргумент не 64-битное целое число."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_FLOAT.key(), "Аргумент не 32-битное дробное число."),
                new LocalizationRecord(MessageKey.INVALID_ARGUMENT_NOT_A_DOUBLE.key(), "Аргумент не 64-битное дробное число."),
                new LocalizationRecord(MessageKey.EMPTY.key(), "")
        );
    }
}
