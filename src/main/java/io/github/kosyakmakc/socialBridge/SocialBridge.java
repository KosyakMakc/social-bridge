package io.github.kosyakmakc.socialBridge;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.*;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SocialBridge implements ISocialBridge {

    public static ISocialBridge INSTANCE;

    private final IMinecraftPlatform minecraftPlatform;
    @SuppressWarnings("rawtypes")
    private final Map<Class, ISocialPlatform> socialPlatforms;
    @SuppressWarnings("rawtypes")
    private final Map<Class, IBridgeModule> bridgeModules;
    private final DatabaseContext databaseContext;

    private final ConfigurationService configurationService;
    private final LocalizationService localizationService;

    private final BridgeEvents events = new BridgeEvents();

    private SocialBridge(IMinecraftPlatform mcPlatform) throws SQLException {
        minecraftPlatform = mcPlatform;
        socialPlatforms = new HashMap<>();
        bridgeModules = new HashMap<>();
        
            
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
        return socialPlatforms.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISocialPlatform> T getSocialPlatform(Class<T> tClass) {
        var platform = socialPlatforms.getOrDefault(tClass, null);
        if (platform != null) {
            return (T) platform;
        }
        else {
            return null;
        }
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

                    socialPlatforms.put(socialPlatform.getClass(), socialPlatform);
                })
                .thenCompose(Void -> connectModulesToSocialPlatorm(socialPlatform))
                .thenCompose(Void -> events.socialPlatformConnect.invoke(socialPlatform))
                .thenApply(Void -> {
                    logger.info("social platform '" + socialPlatform.getPlatformName() + "' connected");
                    return true;
                });
        }
        else {
            logger.severe("social platform '" + socialPlatform.getPlatformName() + "' have incompatible social-bridge API, ignoring it...");
            return CompletableFuture.completedFuture(false);
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

        if (socialPlatforms.remove(socialPlatform.getClass(), socialPlatform)) {
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
    public CompletableFuture<Boolean> connectModule(IBridgeModule module) {
        var logger = getLogger();
        logger.info("Registering module '" + module.getName() + "' (" +  module.getCompabilityVersion().toString() + ")");

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
                .thenCompose(Void -> connectSocialCommands(module))
                .thenCompose(Void -> connectMinecraftCommands(module))
                .thenCompose(Void -> connectModuleToMinecraftPlatform(module))
                .thenCompose(Void -> connectModuleToSocialPlatforms(module))
                .thenCompose(Void -> events.moduleConnect.invoke(module))
                .thenApply(Void -> {
                    bridgeModules.put(module.getClass(), module);
                    logger.info("module '" + module.getName() + "' connected");
                    return true;
                });
        }
        else {
            logger.severe("module '" + module.getName() + "' have incompatible social-bridge API, ignoring it...");
            return CompletableFuture.completedFuture(false);
        }
    }

    private CompletableFuture<Void> connectModuleToSocialPlatforms(IBridgeModule module) {
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

    private CompletableFuture<Void> connectModuleToMinecraftPlatform(IBridgeModule module) {
        return minecraftPlatform.connectModule(module);
    }

    private CompletableFuture<Void> connectMinecraftCommands(IBridgeModule module) {
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

    private CompletableFuture<Void> connectSocialCommands(IBridgeModule module) {
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
    public CompletableFuture<Void> disconnectModule(IBridgeModule module) {
        var logger = getLogger();
        logger.info("disconnect module '" + module.getName() + "' (" +  module.getCompabilityVersion().toString() + ")");

        if (bridgeModules.remove(module.getClass(), module)) {
            return disconnectModuleFromSocialPlatforms(module)
                .thenCompose(Void -> disconnectMinecraftCommands(module))
                .thenCompose(Void -> disconnectSocialCommands(module))
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

    private CompletableFuture<Void> disconnectSocialCommands(IBridgeModule module) {
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

    private CompletableFuture<Void> disconnectMinecraftCommands(IBridgeModule module) {
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

    private CompletableFuture<Void> disconnectModuleFromSocialPlatforms(IBridgeModule module) {
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
    public Collection<IBridgeModule> getModules() {
        return bridgeModules.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBridgeModule> T getModule(Class<T> tClass) {
        var module = bridgeModules.getOrDefault(tClass, null);
        if (module != null) {
            return (T) module;
        }
        else {
            return null;
        }
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
