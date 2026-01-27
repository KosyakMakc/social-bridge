package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.Migrations.IMigration;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Migrations.v1_InitialSetup;
import io.github.kosyakmakc.socialBridge.ISocialBridge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public class ApplyDatabaseMigrations implements Consumer<ISocialBridge> {
    /**
     * list of migration for database, MUST be sorted from MIN to MAX versions
     */
    public final IMigration[] migrations = new IMigration[] {
            new v1_InitialSetup()
    };

    @Override
    public void accept(ISocialBridge bridge) {
        var logger = bridge.getLogger();
        var configurationService = bridge.getConfigurationService();

        var databaseVersion = configurationService.getDatabaseVersion(null).join();
        var latestDatabaseVersion = Arrays.stream(migrations).max(Comparator.comparingInt(IMigration::getVersion)).get().getVersion();

        if (databaseVersion < latestDatabaseVersion) {
            logger.info("current database version is outdated, version: " + databaseVersion);
            for (var migration : migrations) {
                if (migration.getVersion() > databaseVersion) {
                    logger.info("applying migration \"" + migration.getName() + "\" (version: " + migration.getVersion() + ").");
                    bridge.doTransaction(migration).join();
                }
            }
            logger.info("current database updated.");
        }
        else if (databaseVersion == latestDatabaseVersion) {
            logger.info("current database version is actual.");
        }
        else {
            logger.warning("Current database version is higher than bundled in plugin.");
            logger.warning("You can continue to use it at your own risk.");
        }
    }
}
