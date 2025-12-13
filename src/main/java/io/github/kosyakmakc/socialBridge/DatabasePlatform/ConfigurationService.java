package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.ConfigRow;
import io.github.kosyakmakc.socialBridge.DefaultModule;
import io.github.kosyakmakc.socialBridge.IBridgeModule;
import io.github.kosyakmakc.socialBridge.IConfigurationService;
import io.github.kosyakmakc.socialBridge.ISocialBridge;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ConfigurationService implements IConfigurationService {
    public static final String DATABASE_VERSION = "DATABASE_VERSION";

    private final ISocialBridge bridge;
    private final Logger logger;

    public ConfigurationService(ISocialBridge bridge) {
        this.bridge = bridge;
        this.logger = Logger.getLogger(bridge.getLogger().getName() + '.' + ConfigurationService.class.getSimpleName());
    }

    @Override
    public CompletableFuture<String> get(IBridgeModule module, String parameter, String defaultValue) {
        return get(module.getId(), parameter, defaultValue);
    }

    @Override
    public CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue) {
        return bridge.queryDatabase(databaseContext -> {
            try {
                var records = databaseContext.configurations.queryBuilder()
                            .where()
                                .eq(ConfigRow.MODULE_FIELD_NAME, moduleId)
                                .and()
                                .eq(ConfigRow.PARAMETER_FIELD_NAME, parameter)
                            .query();
                if (records.size() > 0) {
                    var record = records.getFirst();
                    return record.getValue();
                }
            } catch (SQLException e) {
                // skip first sql query to not existed table
                if (!e.getMessage().equals("[SQLITE_ERROR] SQL error or missing database (no such table: config)")) {
                    e.printStackTrace();
                }
            }
            return defaultValue;
        });
    }

    @Override
    public CompletableFuture<Boolean> set(IBridgeModule module, String parameter, String value) {
        return set(module.getId(), parameter, value)
        .thenApply(status -> {
            if (status) {
                logger.info("database configuration change: " + module.getName() + "." + parameter + "=" + value);
            }
            return status;
        });
    }

    public CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value) {
        if (parameter.isBlank()) {
            throw new RuntimeException("Empty parameter name is not allowed");
        }

        return bridge.queryDatabase(databaseContext -> {
            try {
                var records = databaseContext.configurations.queryBuilder()
                            .where()
                                .eq(ConfigRow.MODULE_FIELD_NAME, moduleId)
                                .and()
                                .eq(ConfigRow.PARAMETER_FIELD_NAME, parameter)
                            .query();
                if (records.size() > 0) {
                    var record = records.getFirst();
                    record.setValue(value);
                    databaseContext.configurations.update(record);
                } else {
                    var newRecord = new ConfigRow(moduleId, parameter, value);
                    databaseContext.configurations.create(newRecord);
                }

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public CompletableFuture<Integer> getDatabaseVersion() {
        return get(DefaultModule.MODULE_ID, DATABASE_VERSION, "")
               .thenApply(rawVersion -> {
                   try {
                       return Integer.parseInt(rawVersion);
                   }
                   catch (NumberFormatException err) {
                       return -1;
                   }
               });
    }
}
