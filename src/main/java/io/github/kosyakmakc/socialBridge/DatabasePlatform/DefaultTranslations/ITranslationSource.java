package io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations;

import java.util.List;

public interface ITranslationSource {
    String getLanguage();
    List<LocalizationRecord> getRecords();
}