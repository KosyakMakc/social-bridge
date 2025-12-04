package io.github.kosyakmakc.socialBridge.DatabasePlatform.Migrations;

import com.j256.ormlite.table.TableUtils;

import io.github.kosyakmakc.socialBridge.DefaultModule;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.ConfigurationService;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DatabaseContext;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.ConfigRow;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.Localization;

import java.sql.SQLException;

public class v1_InitialSetup implements IMigration {
    @Override
    public String getName() { return "InitialSetup"; }

    @Override
    public int getVersion() { return 1; }

    @Override
    public Void accept(DatabaseContext databaseContext) {
        try {
            var connectionSource = databaseContext.getConnectionSource();
            
            TableUtils.createTableIfNotExists(connectionSource, ConfigRow.class);
            TableUtils.createTableIfNotExists(connectionSource, Localization.class);

            var parameter = ConfigurationService.DATABASE_VERSION;
            var value = Integer.toString(getVersion());
            
            var record = databaseContext.configurations.queryForId(parameter);
            if (record != null) {
                record.setValue(value);
                databaseContext.configurations.update(record);
            } else {
                var newRecord = new ConfigRow(DefaultModule.MODULE_ID, parameter, value);
                databaseContext.configurations.create(newRecord);
            }
        }
        catch (SQLException error) {
            throw new RuntimeException(error);
        }

        return null;
    }
}
