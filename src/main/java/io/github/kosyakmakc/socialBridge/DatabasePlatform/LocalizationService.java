package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.Localization;
import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
import io.github.kosyakmakc.socialBridge.Modules.ITranslationsModule;
import io.github.kosyakmakc.socialBridge.ITransaction;
import io.github.kosyakmakc.socialBridge.ILocalizationService;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalizationService implements ILocalizationService {
    static public final String defaultLocale = Locale.US.getLanguage();
    private final Logger logger;
    private final ISocialBridge bridge;

    private ConcurrentHashMap<UUID, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> inMemoryCache = new ConcurrentHashMap<>();

    public LocalizationService(ISocialBridge bridge) {
        this.bridge = bridge;
        logger = Logger.getLogger(bridge.getLogger().getName() + '.' + LocalizationService.class.getSimpleName());
    }

    @Override
    public CompletableFuture<String> getMessage(IModuleBase module, String locale, MessageKey key, ITransaction transaction) {
        var localization = searchByCache(module, key.key(), key);
        if (localization != null) {
            return CompletableFuture.completedFuture(localization);
        }

        return getMessageFromStorage(module, locale, key, transaction);
    }

    private CompletableFuture<String> getMessageFromStorage(IModuleBase module, String locale, MessageKey key, ITransaction transaction) {
        return transaction == null
            ? bridge.doTransaction(transaction2 -> getMessageFromDatabase(module, locale, key, transaction2))
            : getMessageFromDatabase(module, locale, key, transaction);
    }

    private CompletableFuture<String> getMessageFromDatabase(IModuleBase module, String locale, MessageKey key, ITransaction transaction) {
            List<Localization> records;
            try {
                records = transaction.getDatabaseContext().localizations.queryBuilder()
                        .where()
                            .eq(Localization.MODULE_FIELD_NAME, module.getId())
                            .and()
                            .eq(Localization.LANGUAGE_FIELD_NAME, locale)
                            .and()
                            .eq(Localization.KEY_FIELD_NAME, key.key())
                        .query();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            CompletableFuture<String> task;
            if (records.size() == 1) {
                task = CompletableFuture.completedFuture(records.getFirst().getLocalization());
            }
            else {
                task = CompletableFuture.completedFuture(null);
            }

            return task.thenCompose(x -> {
                if (x == null) {
                    if (!locale.equalsIgnoreCase(defaultLocale)) {
                        return getMessage(module, defaultLocale, key, transaction);
                    }
                }
                return CompletableFuture.completedStage(x);
            })
            .thenApply(x -> {
                var localization2 = x != null ? x : key.key();
                appendToCache(module, locale, key, localization2);
                return localization2;
            })
            .exceptionally(error -> {
                logger.log(Level.SEVERE, "failed localization search", error);
                return "internal database error";
            });
    }

    private String searchByCache(IModuleBase module, String locale, MessageKey key) {
        var moduleCache = inMemoryCache.getOrDefault(module.getId(), null);
        if (moduleCache == null) {
            moduleCache = new ConcurrentHashMap<>();
            inMemoryCache.put(module.getId(), moduleCache);
        }
        var languageCache = moduleCache.getOrDefault(locale, null);
        if (languageCache == null) {
            languageCache = new ConcurrentHashMap<>();
            moduleCache.put(locale, languageCache);
        }
        var localization = languageCache.getOrDefault(key.key(), null);
        if (localization != null) {
            return localization;
        }
        else {
            return null;
        }
    }

    private void appendToCache(IModuleBase module, String locale, MessageKey key,  String localization) {
        var moduleCache = inMemoryCache.getOrDefault(module.getId(), null);
        if (moduleCache == null) {
            moduleCache = new ConcurrentHashMap<>();
            inMemoryCache.put(module.getId(), moduleCache);
        }
        var languageCache = moduleCache.getOrDefault(locale, null);
        if (languageCache == null) {
            languageCache = new ConcurrentHashMap<>();
            moduleCache.put(locale, languageCache);
        }
        var existedLocalization = languageCache.getOrDefault(key.key(), null);
        if (existedLocalization == null) {
            languageCache.put(key.key(), localization);
        }
    }

    @Override
    public CompletableFuture<Boolean> setMessage(IModuleBase module, String locale, MessageKey key, String localization, ITransaction transaction) {
        return transaction == null
            ? bridge.doTransaction(transaction2 -> setMessageToDatabase(module, locale, key, localization, transaction2))
            : setMessageToDatabase(module, locale, key, localization, transaction);
    }

    private CompletableFuture<Boolean> setMessageToDatabase(IModuleBase module, String locale, MessageKey key, String localization, ITransaction transaction) {
        var moduleId = module.getId();
        logger.info("update localization for module '" + module.getName() + "' (lang=" + locale + " key=" + key.key() + ")");

        try {
            var databaseContext = transaction.getDatabaseContext();

            var records = databaseContext.localizations.queryBuilder()
                .where()
                    .eq(Localization.MODULE_FIELD_NAME, module.getId())
                    .and()
                    .eq(Localization.LANGUAGE_FIELD_NAME, locale)
                    .and()
                    .eq(Localization.KEY_FIELD_NAME, key.key())
                .query();

            if (records.isEmpty()) {
                databaseContext.localizations.create(
                    new Localization(
                        moduleId,
                        locale,
                        key.key(),
                        localization
                    )
                );
                appendToCache(module, locale, key, localization);
            }
            else {
                var localizationRecord = records.getFirst();
                localizationRecord.setLocalization(localization);
                databaseContext.localizations.update(localizationRecord);
                appendToCache(module, locale, key, localization);
            }

            return CompletableFuture.completedFuture(true);
        }
        catch (Exception error) {
            error.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<Void> restoreLocalizationsOfModule(ITranslationsModule module) {
        var moduleId = module.getId();
        logger.info("restoring localizations for module '" + module.getName() + "'");
        
        if (!inMemoryCache.containsKey(moduleId)) {
            inMemoryCache.put(moduleId, new ConcurrentHashMap<>());
        }

        return CompletableFuture.allOf(
            module
                .getTranslations()
                .stream()
                .map(x -> restoreLocalizationSource(x, moduleId))
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> restoreLocalizationSource (ITranslationSource source, UUID moduleId) {
        AtomicInteger insertedRecords = new AtomicInteger();
        var records = source.getRecords();
        var sourceRecords = records.size();

        var language = source.getLanguage();

        if (language.isBlank()) {
            throw new RuntimeException("ITranslationSource.getLanguage() must be a not blank");
        }
        
        var moduleCache = inMemoryCache.get(moduleId);
        if (!moduleCache.containsKey(language)) {
            moduleCache.put(language, new ConcurrentHashMap<>());
        }
        var languageCache = moduleCache.get(language);

        return CompletableFuture.allOf(
            records
                .stream()
                .map(record -> {
                    if (record.key().isBlank()) {
                        throw new RuntimeException("LocalizationRecord.Key() must be a not blank");
                    }

                    return bridge.doTransaction(transaction -> {
                        try {
                            transaction.getDatabaseContext().localizations.create(
                                new Localization(
                                    moduleId,
                                    source.getLanguage(),
                                    record.key(),
                                    record.localization()
                                )
                            );
                            insertedRecords.getAndIncrement();
                            languageCache.put(record.key(), record.localization());
                        }
                        catch (SQLException error) {
                            if (error.getCause() instanceof SQLException innerException
                             && innerException.getMessage().equals("[SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: localization.module, localization.language, localization.key)")) {
                                // do nothing
                            }
                            else {
                                error.printStackTrace();
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    });
                })
                .toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    if (insertedRecords.get() > 0) {
                        logger.info("restored localization source '" + source.getLanguage() + "' in database (" + sourceRecords + "/" + insertedRecords.get() + ")");
                    }
                });
    }
}
