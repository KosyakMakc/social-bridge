package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.util.LinkedList;
import java.util.List;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.LocalizationRecord;

public class TranslationSourceForTest implements ITranslationSource {
    private String language;
    private LinkedList<LocalizationRecord> records = new LinkedList<>();

    public TranslationSourceForTest (String language) {
        this.language = language;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public List<LocalizationRecord> getRecords() {
        return records;
    }

    public void Append(String key, String localization) {
        records.add(new LocalizationRecord(key, localization));
    }

}
