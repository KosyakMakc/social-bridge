package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.ConfigRow;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.IDatabaseTable;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.Localization;
import io.github.kosyakmakc.socialBridge.ISocialBridge;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseContext {
    public Dao<ConfigRow, String> configurations;
    public Dao<Localization, Integer> localizations;

    @SuppressWarnings("rawtypes")
    private final HashMap<Class, Dao> extensionTables = new HashMap<>();

    private final ConnectionSource connectionSource;
    private final TransactionManager transactionManager;
    private final Logger logger;

    private final Executor singleExecutor = Executors.newSingleThreadExecutor();

    public DatabaseContext(ISocialBridge bridge, JdbcConnectionSource connectionSource) throws SQLException {
        logger = Logger.getLogger(bridge.getLogger().getName() + '.' + DatabaseContext.class.getSimpleName());

        logger.info(connectionSource.getUrl());
        logger.info("database inits...");

        this.connectionSource = connectionSource;
        this.transactionManager = new TransactionManager(connectionSource);

        configurations = DaoManager.createDao(connectionSource, ConfigRow.class);
        localizations = DaoManager.createDao(connectionSource, Localization.class);
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    public <T> CompletableFuture<T> withTransaction(Callable<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return transactionManager.callInTransaction(action);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, singleExecutor);
    }

    public <T extends IDatabaseTable, Key> Dao<T, Key> registerTable(Class<? extends IDatabaseTable> tableClass) {
        try {
            var dao = DaoManager.createDao(connectionSource, tableClass);
            extensionTables.put(tableClass, dao);
            return (Dao<T, Key>) dao;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed register extension table", e);
            return null;
        }
    }

    public <T extends IDatabaseTable, Key> Dao<T, Key> getDaoTable(Class<T> tableClass) {
        var table = extensionTables.getOrDefault(tableClass, null);
        if (table != null) {
            return (Dao<T, Key>) table;
        }
        else {
            return null;
        }
    }
}
