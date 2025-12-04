package io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = ConfigRow.TABLE_NAME)
public class ConfigRow implements IDatabaseTable {
    public static final String TABLE_NAME = "config";

    public static final String ID_FIELD_NAME = "id";
    public static final String MODULE_FIELD_NAME = "module_uuid";
    public static final String PARAMETER_FIELD_NAME = "parameter";
    public static final String VALUE_FIELD_NAME = "value";

    public static final String FULL_KEY_INDEX_NAME = "full_key_idx";

    @DatabaseField(columnName = ID_FIELD_NAME, id = true)
    private int id;

    @DatabaseField(columnName = MODULE_FIELD_NAME, uniqueIndexName = FULL_KEY_INDEX_NAME)
    private UUID module;

    @DatabaseField(columnName = PARAMETER_FIELD_NAME, uniqueIndexName = FULL_KEY_INDEX_NAME)
    private String parameter;

    @DatabaseField(columnName = VALUE_FIELD_NAME)
    private String value;

    public ConfigRow() {

    }

    public ConfigRow(UUID module, String parameter, String value) {
        this.module = module;
        this.parameter = parameter;
        this.value = value;
    }

    public UUID getModule() {
        return module;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
