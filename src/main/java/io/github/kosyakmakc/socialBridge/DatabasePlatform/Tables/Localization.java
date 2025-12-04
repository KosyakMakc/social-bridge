package io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Localization.TABLE_NAME)
public class Localization implements IDatabaseTable {
    public static final String TABLE_NAME = "localization";

    public static final String ID_FIELD_NAME = "id";
    public static final String MODULE_FIELD_NAME = "module";
    public static final String LANGUAGE_FIELD_NAME = "language";
    public static final String KEY_FIELD_NAME = "key";
    public static final String LOCALIZATION_FIELD_NAME = "localization";

    public static final String LANGUAGE_KEY_INDEX_NAME = "language_key_idx";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    private int id;

    @DatabaseField(columnName = MODULE_FIELD_NAME, uniqueIndexName = LANGUAGE_KEY_INDEX_NAME)
    private UUID module;

    @DatabaseField(columnName = LANGUAGE_FIELD_NAME, uniqueIndexName = LANGUAGE_KEY_INDEX_NAME)
    private String language;

    @DatabaseField(columnName = KEY_FIELD_NAME, uniqueIndexName = LANGUAGE_KEY_INDEX_NAME)
    private String key;

    @DatabaseField(columnName = LOCALIZATION_FIELD_NAME)
    private String localization;

    public Localization() {

    }

    public Localization(UUID module, String language, String key, String localization) {
        this.module = module;
        this.language = language;
        this.key = key;
        this.localization = localization;
    }

    public int getId() {
        return id;
    }

    public UUID getModule() {
        return module;
    }

    public String getLanguage() {
        return language;
    }

    public String getKey() {
        return key;
    }

    public String getLocalization() {
        return localization;
    }
}
