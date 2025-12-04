package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.Tables.Localization;
import io.github.kosyakmakc.socialBridge.IBridgeModule;
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

public class LocalizationService {
    static public final String defaultLocale = Locale.US.getLanguage();
    private final Logger logger;
    private final ISocialBridge bridge;

    private ConcurrentHashMap<UUID, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> inMemoryCache = new ConcurrentHashMap<>();

    public LocalizationService(ISocialBridge bridge) {
        this.bridge = bridge;
        logger = Logger.getLogger(bridge.getLogger().getName() + '.' + LocalizationService.class.getSimpleName());
    }

    public String getMessage(IBridgeModule module, String locale, MessageKey key) {
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

        try {
            localization = bridge.queryDatabase(databaseContext -> {
                List<Localization> records;
                try {
                    records = databaseContext.localizations.queryBuilder()
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

                if (records.size() == 1) {
                    return records.getFirst().getLocalization();
                }
                else {
                    return null;
                }
            }).thenApply(x -> {
                if (x == null) {
                    if (!locale.equalsIgnoreCase(defaultLocale)) {
                        return getMessage(module, defaultLocale, key);
                    }
                }
                return x;
            })
            .thenApply(x -> {
                return x != null ? x : key.key();
            })
            .join();

            languageCache.put(key.key(), localization);
            return localization;
        }
        catch (Exception error) {
            logger.log(Level.SEVERE, "failed localization search", error);
            return "internal database error";
            
        }
    }

    public CompletableFuture<Void> restoreLocalizationsOfModule(IBridgeModule module) {
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
        
        var moduleCache = inMemoryCache.get(moduleId);
        if (!moduleCache.containsKey(source.getLanguage())) {
            moduleCache.put(source.getLanguage(), new ConcurrentHashMap<>());
        }
        var languageCache = moduleCache.get(source.getLanguage());

        return CompletableFuture.allOf(
            records
                .stream()
                .map(record ->
                    bridge.queryDatabase(databaseContext -> {
                        try {
                            databaseContext.localizations.create(
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
                            // TODO ignore unique index error
                            error.printStackTrace();
                        }
                        return null;
                    }))
                .toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    if (insertedRecords.get() > 0) {
                        logger.info("restored localization source '" + source.getLanguage() + "' in database (" + sourceRecords + "/" + insertedRecords.get() + ")");
                    }
                });
    }
}
