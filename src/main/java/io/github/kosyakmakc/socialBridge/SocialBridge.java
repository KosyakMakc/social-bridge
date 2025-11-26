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
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    private boolean isStarted = false;

    private SocialBridge(IMinecraftPlatform mcPlatform) throws SQLException {
        minecraftPlatform = mcPlatform;
        socialPlatforms = new HashMap<>();
        bridgeModules = new HashMap<>();
        bridgeModules.put(DefaultModule.class, new DefaultModule());

        var connectionString = mcPlatform.get("connectionString", null);
        if (connectionString == null) {
            throw new RuntimeException("failed connect to database, check connectionString in config");
        }
        databaseContext = new DatabaseContext(this, new JdbcConnectionSource(connectionString));

        configurationService = new ConfigurationService(this);
        localizationService = new LocalizationService(this);

        new ApplyDatabaseMigrations().accept(this);

        minecraftPlatform.setAuthBridge(this);
    }

    @Override
    public void Start() {
        if (isStarted) {
            throw new RuntimeException("Social bridge already running");
        }
        localizationService.restoreDatabase();
        isStarted = true;
        getLogger().info("Social platforms(" + socialPlatforms.size() + "):");
        for (ISocialPlatform socialPlatform : socialPlatforms.values()) {
            getLogger().info("\t\t" + socialPlatform.getPlatformName());
            socialPlatform.Start();
        }

        var totalMcCommands = getModules().stream().mapToInt(x -> x.getMinecraftCommands().size()).sum();
        getLogger().info("Minecraft commands(" + totalMcCommands + "):");
        for (var module : getModules()) {
            for (var mcCommand : module.getMinecraftCommands()) {
                getLogger().info("\t\t/" + module.getName() + ' ' + mcCommand.getLiteral() + ' ' + mcCommand.getArgumentDefinitions().stream().map(x -> '{' + x.getName() + '}').collect(Collectors.joining(" ")));
                mcCommand.init(this);
            }
        }
        var totalSocialCommands = getModules().stream().mapToInt(x -> x.getSocialCommands().size()).sum();
        getLogger().info("Social commands(" + totalSocialCommands + "):");
        for (var module : getModules()) {
            for (var socialCommand : module.getSocialCommands()) {
                getLogger().info("\t\t/" + module.getName() + '-' + socialCommand.getLiteral() + ' ' + socialCommand.getArgumentDefinitions().stream().map(x -> '{' + x.getName() + '}').collect(Collectors.joining(" ")));
                socialCommand.init(this);
            }
        }
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
    public void queryDatabase(IDatabaseConsumer action) throws SQLException {
        synchronized (databaseContext) {
            databaseContext.withTransaction(() -> action.accept(databaseContext) );
        }
    }

    @Override
    public Collection<ISocialPlatform> getSocialPlatforms() {
        return socialPlatforms.values();
    }

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
    public boolean registerSocialPlatform(ISocialPlatform socialPlatform) {
        if (isStarted) {
            throw new RuntimeException("Social bridge already running, register your platform on startup please");
        }

        var logger = getLogger();
        logger.info("Registering social platform - " + socialPlatform.getPlatformName() + "(" +  socialPlatform.getCompabilityVersion().toString() + ")");

        var rootVersion = getVersion();
        var childVersion = socialPlatform.getCompabilityVersion();
        if (rootVersion.isCompatible(childVersion)) {
            socialPlatforms.put(socialPlatform.getClass(), socialPlatform);
            socialPlatform.setAuthBridge(this);
            logger.warning(socialPlatform.getPlatformName() + " connected");
            return true;
        }
        else {
            logger.warning(socialPlatform.getPlatformName() + " have incompatible social-bridge API, ignoring it...");
            return false;
        }
    }

    @Override
    public boolean registerModule(IBridgeModule module) {
        if (isStarted) {
            throw new RuntimeException("Social bridge already running, register your module on startup please");
        }

        var logger = getLogger();
        logger.info("Registering module - " + module.getName() + "(" +  module.getCompabilityVersion().toString() + ")");

        var rootVersion = getVersion();
        var childVersion = module.getCompabilityVersion();
        if (rootVersion.isCompatible(childVersion)) {
            if (module.init(this)) {
                bridgeModules.put(module.getClass(), module);
                logger.info(module.getName() + " connected");
                return true;
            }
            else {
                logger.warning(module.getName() + " init failed");
                return false;
            }
        }
        else {
            logger.warning(module.getName() + " have incompatible social-bridge API, ignoring it...");
            return false;
        }
    }

    @Override
    public Collection<IBridgeModule> getModules() {
        return bridgeModules.values();
    }

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
}
