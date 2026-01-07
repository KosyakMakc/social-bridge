package io.github.kosyakmakc.socialBridge;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.*;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SocialBridge implements ISocialBridge {
    public static ISocialBridge INSTANCE;

    private final IMinecraftPlatform minecraftPlatform;
    @SuppressWarnings("rawtypes")
    private final Map<Class, ISocialPlatform> socialPlatformsByClass;
    private final Map<UUID, ISocialPlatform> socialPlatformsById;
    @SuppressWarnings("rawtypes")
    private final Map<Class, ISocialModule> modulesByClass;
    private final Map<UUID, ISocialModule> modulesById;
    private final DatabaseContext databaseContext;

    private final ConfigurationService configurationService;
    private final LocalizationService localizationService;

    private final BridgeEvents events = new BridgeEvents();

    private SocialBridge(IMinecraftPlatform mcPlatform) throws SQLException {
        LoggerFactory.setLogBackendFactory(LogBackendType.NULL);

        minecraftPlatform = mcPlatform;
        socialPlatformsByClass = new HashMap<>();
        socialPlatformsById = new HashMap<>();
        modulesByClass = new HashMap<>();
        modulesById = new HashMap<>();

        var defaultModule = new DefaultModule(mcPlatform);

        var connectionString = mcPlatform.get(defaultModule, "connectionString", null).join();
        if (connectionString == null) {
            throw new RuntimeException("failed connect to database, check connectionString in config");
        }
        databaseContext = new DatabaseContext(this, new JdbcConnectionSource(connectionString));
        
        configurationService = new ConfigurationService(this);
        localizationService = new LocalizationService(this);
        new ApplyDatabaseMigrations().accept(this);

        this.connectModule(defaultModule).join();
    }

    @Override
    public Logger getLogger() {
        return minecraftPlatform.getLogger();
    }

    @Override
    public LocalizationService getLocalizationService() {
        return localizationService;
    }

    @Override
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Override
    public <T> CompletableFuture<T> queryDatabase(IDatabaseConsumer<T> action) {
        return databaseContext.withTransaction(() -> action.accept(databaseContext) );
    }

    @Override
    public Collection<ISocialPlatform> getSocialPlatforms() {
        return socialPlatformsByClass.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISocialPlatform> T getSocialPlatform(Class<T> tClass) {
        var platform = socialPlatformsByClass.getOrDefault(tClass, null);
        if (platform != null) {
            return (T) platform;
        }
        else {
            return null;
        }
    }

    @Override
    public ISocialPlatform getSocialPlatform(UUID socialPlatformId) {
        return socialPlatformsById.getOrDefault(socialPlatformId, null);
    }

    @Override
    public IMinecraftPlatform getMinecraftPlatform() {
        return minecraftPlatform;
    }

    public static void Init(IMinecraftPlatform minecraftPlatform) throws SQLException, IOException {
        if (INSTANCE != null) {
            throw new RuntimeException("Social bridge MUST BE single instance");
        }
        INSTANCE = new SocialBridge(minecraftPlatform);
    }

    @Override
    public CompletableFuture<Boolean> connectSocialPlatform(ISocialPlatform socialPlatform) {
        var logger = getLogger();
        logger.info("connect social platform '" + socialPlatform.getPlatformName() + "' (" +  socialPlatform.getCompabilityVersion().toString() + ")");
        
        ValidateAndThrowSocialPlatform(socialPlatform);

        var rootVersion = getVersion();
        var childVersion = socialPlatform.getCompabilityVersion();
        if (rootVersion.isCompatible(childVersion)) {
            return socialPlatform
                .enable(this)
                .thenAccept(enableStatus -> {
                    if (!enableStatus) {
                        logger.warning("social platform '" + socialPlatform.getPlatformName() + "' canceled the activation, ignoring it...");
                        throw new CancellationException("social platform '" + socialPlatform.getPlatformName() + "' canceled the activation");
                    }

                    socialPlatformsByClass.put(socialPlatform.getClass(), socialPlatform);
                    socialPlatformsById.put(socialPlatform.getId(), socialPlatform);
                })
                .thenCompose(Void -> connectModulesToSocialPlatorm(socialPlatform))
                .thenCompose(Void -> events.socialPlatformConnect.invoke(socialPlatform))
                .thenApply(Void -> {
                    logger.info("social platform '" + socialPlatform.getPlatformName() + "' connected");
                    return true;
                });
        }
        else {
            logger.severe("social platform '" + socialPlatform.getPlatformName() + "' have incompatible SocialBridge API, ignoring it...");
            return CompletableFuture.completedFuture(false);
        }
    }

    private static final Pattern socialPlatformNameValidation = Pattern.compile("[\s\\\"\'`]"); // no whitespaces, escape symbol and quotas

    private void ValidateAndThrowSocialPlatform(ISocialPlatform socialPlatform) {
        var name = socialPlatform.getPlatformName();
        var matcher = socialPlatformNameValidation.matcher(name);
        if (matcher.find()) {
            throw new RuntimeException("Invalid social platform name, please don't use whitespaces, escape symbol and quotas");
        }

        if (socialPlatformsByClass.getOrDefault(socialPlatform.getClass(), null) != null) {
            throw new RuntimeException("Duplication java class of social platform detected");
        }

        if (socialPlatformsById.getOrDefault(socialPlatform.getId(), null) != null) {
            throw new RuntimeException("Duplication social platform UUID detected");
        }
    }

    private CompletableFuture<Void> connectModulesToSocialPlatorm(ISocialPlatform socialPlatform) {
        return CompletableFuture.allOf(
            getModules()
            .stream()
            .map(module -> socialPlatform
                .connectModule(module)
                .handle((Void2, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                    }
                    return null;
                }))
            .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> disconnectSocialPlatform(ISocialPlatform socialPlatform) {
        var logger = getLogger();
        logger.info("disconnect social platform '" + socialPlatform.getPlatformName() + "' (" +  socialPlatform.getCompabilityVersion().toString() + ")");

        if (socialPlatformsByClass.remove(socialPlatform.getClass(), socialPlatform)) {
            socialPlatformsById.remove(socialPlatform.getId(), socialPlatform);
            return socialPlatform
                .disable()
                .thenCompose(Void -> events.socialPlatformDisconnect.invoke(socialPlatform))
                .thenRun(() -> {
                    logger.info("social platform '" + socialPlatform.getPlatformName() + "' disconnected");
                });
        }
        else {
            logger.severe("social platform '" + socialPlatform.getPlatformName() + "' is not registered before");
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Boolean> connectModule(ISocialModule module) {
        var logger = getLogger();
        logger.info("Registering module '" + module.getName() + "' (" +  module.getCompabilityVersion().toString() + ")");

        ValidateAndThrowModule(module);

        var rootVersion = getVersion();
        var childVersion = module.getCompabilityVersion();
        if (rootVersion.isCompatible(childVersion)) {
            return module
                .enable(this)
                .thenAccept(enableStatus -> {
                    if (!enableStatus) {
                        logger.warning("module '" + module.getName() + "' canceled the activation, ignoring it...");
                        throw new CancellationException("module '" + module.getName() + "' canceled the activation");
                    }
                })
                .thenCompose(Void -> localizationService.restoreLocalizationsOfModule(module))
                .thenCompose(Void -> enableSocialCommands(module))
                .thenCompose(Void -> enableMinecraftCommands(module))
                .thenCompose(Void -> connectModuleToMinecraftPlatform(module))
                .thenCompose(Void -> connectModuleToSocialPlatforms(module))
                .thenCompose(Void -> events.moduleConnect.invoke(module))
                .thenApply(Void -> {
                    modulesByClass.put(module.getClass(), module);
                    modulesById.put(module.getId(), module);
                    logger.info("module '" + module.getName() + "' connected");
                    return true;
                });
        }
        else {
            logger.severe("module '" + module.getName() + "' have incompatible SocialBridge API, ignoring it...");
            return CompletableFuture.completedFuture(false);
        }
    }

    // no whitespaces, dash symbol, dot symbol
    private static final Pattern moduleNameValidation = Pattern.compile("[\\s\\-\\.]");
    
    // no whitespaces, escape symbol, dash symbol, dot symbol and quotas
    private static final Pattern socialCommandNameValidation = Pattern.compile("[\\s]");
    // no whitespaces, escape symbol, dot symbol and quotas
    private static final Pattern minecraftCommandNameValidation = Pattern.compile("[\\s]");

    // 2 char template in lower case: en, de, cz, ru, ua, ja and etc
    private static final Pattern translationLanguageValidation = Pattern.compile("^[a-z]{2}$");
    // Simple_Word_Identifier
    private static final Pattern translationKeyValidation = Pattern.compile("^[a-zA-Z_]+$");

    private void ValidateAndThrowModule(ISocialModule module) {
        var name = module.getName();
        for (var existedModule : getModules()) {
            if (existedModule.getName().equals(name)) {
                throw new RuntimeException("Duplication module name detected");
            }
        }

        var matcher1 = moduleNameValidation.matcher(name);
        if (matcher1.find()) {
            throw new RuntimeException("Invalid module name, please don't use whitespaces, escape symbol, dash symbol, dot symbol and quotas");
        }

        if (modulesByClass.getOrDefault(module.getClass(), null) != null) {
            throw new RuntimeException("Duplication java class of module detected");
        }

        if (modulesById.getOrDefault(module.getId(), null) != null) {
            throw new RuntimeException("Duplication module UUID detected");
        }

        for (var socialCommand : module.getSocialCommands()) {
            var matcher2 = socialCommandNameValidation.matcher(socialCommand.getLiteral());
            if (matcher2.find()) {
                throw new RuntimeException("Invalid social command name, please don't use whitespaces, escape symbol, dash symbol, dot symbol and quotas");
            }
        }

        for (var minecraftCommand : module.getMinecraftCommands()) {
            var matcher3 = minecraftCommandNameValidation.matcher(minecraftCommand.getLiteral());
            if (matcher3.find()) {
                throw new RuntimeException("Invalid social command name, please don't use whitespaces, escape symbol, dot symbol and quotas");
            }
        }

        for (var translationSource : module.getTranslations()) {
            var matcher4 = translationLanguageValidation.matcher(translationSource.getLanguage());
            if (!matcher4.find()) {
                throw new RuntimeException("Invalid translation language name, please use ISO 639 (2 symbols of [a-z])");
            }

            for (var record : translationSource.getRecords()) {
                var matcher5 = translationKeyValidation.matcher(record.key());
                if (!matcher5.find()) {
                    throw new RuntimeException("Invalid translation key name, please use [a-zA-Z_] symbols");
                }
            }
        }
    }

    private CompletableFuture<Void> connectModuleToSocialPlatforms(ISocialModule module) {
        return CompletableFuture.allOf(
            getSocialPlatforms()
            .stream()
            .map(socialPlatform -> socialPlatform
                .connectModule(module)
                .handle((Void2, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                    }
                    return null;
                }))
            .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> connectModuleToMinecraftPlatform(ISocialModule module) {
        return minecraftPlatform.connectModule(module);
    }

    private CompletableFuture<Void> enableMinecraftCommands(ISocialModule module) {
        return CompletableFuture.allOf(
            module
                .getMinecraftCommands()
                .stream()
                .map(command -> command
                    .enable(module)
                    .handle((Void2, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        return null;
                    }))
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> enableSocialCommands(ISocialModule module) {
        return CompletableFuture.allOf(
            module
                .getSocialCommands()
                .stream()
                .map(command -> command
                    .enable(module)
                    .handle((Void2, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        return null;
                    }))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> disconnectModule(ISocialModule module) {
        var logger = getLogger();
        logger.info("disconnect module '" + module.getName() + "' (" +  module.getCompabilityVersion().toString() + ")");

        if (modulesByClass.remove(module.getClass(), module)) {
            modulesById.remove(module.getId(), module);
            return disconnectModuleFromSocialPlatforms(module)
                .thenCompose(Void -> disableMinecraftCommands(module))
                .thenCompose(Void -> disableSocialCommands(module))
                .thenApply(Void -> module.disable())
                .thenCompose(Void -> events.moduleDisconnect.invoke(module))
                .thenRun(() -> {
                    logger.info("module '" + module.getName() + "' disconnected");
                });
        }
        else {
            logger.severe("module '" + module.getName() + "' is not registered before");
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> disableSocialCommands(ISocialModule module) {
        return CompletableFuture.allOf(
            module
                .getSocialCommands()
                .stream()
                .map(command -> command
                    .disable()
                    .handle((Void2, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        return null;
                    }))
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> disableMinecraftCommands(ISocialModule module) {
        return CompletableFuture.allOf(
            module
                .getMinecraftCommands()
                .stream()
                .map(command -> command
                    .disable()
                    .handle((Void2, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        return null;
                    }))
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> disconnectModuleFromSocialPlatforms(ISocialModule module) {
        return CompletableFuture
            .allOf(
                getSocialPlatforms()
                .stream()
                .map(socialPlatform -> socialPlatform
                    .disconnectModule(module)
                    .handle((Void, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        return null;
                    })
                )
                .toArray(CompletableFuture[]::new)
            );
    }

    @Override
    public Collection<ISocialModule> getModules() {
        return modulesByClass.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISocialModule> T getModule(Class<T> tClass) {
        var module = modulesByClass.getOrDefault(tClass, null);
        if (module != null) {
            return (T) module;
        }
        else {
            return null;
        }
    }
    @Override
    public ISocialModule getModule(UUID moduleId) {
        return modulesById.getOrDefault(moduleId, null);
    }

    @Override
    public Version getVersion() {
        return minecraftPlatform.getSocialBridgeVersion();
    }

    @Override
    public BridgeEvents getEvents() {
        return events;
    }
}
