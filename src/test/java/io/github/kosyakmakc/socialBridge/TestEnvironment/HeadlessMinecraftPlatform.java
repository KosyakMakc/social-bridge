package io.github.kosyakmakc.socialBridge.TestEnvironment;

import io.github.kosyakmakc.socialBridge.DefaultModule;
import io.github.kosyakmakc.socialBridge.ISocialModule;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.Utils.Version;
import io.github.kosyakmakc.socialBridge.SocialBridge;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class HeadlessMinecraftPlatform implements IMinecraftPlatform {
    public static final String PLATFORM_NAME = "headless";
    public static final UUID PLATFORM_ID = UUID.fromString("c936579c-da7e-47dd-be85-d93f0558fab1");

    public static final Version VERSION = new Version("0.6.3");
    private LinkedBlockingQueue<ISocialModule> registeredModules = new LinkedBlockingQueue<>();
    private HashMap<UUID, HashMap<String, String>> config = new HashMap<>();
    private final UUID instanceId = UUID.randomUUID();

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public UUID getId() {
        return PLATFORM_ID;
    }

    @Override
    public UUID getInstanceId() {
        return instanceId;
    }

    @Override
    public Path getDataDirectory() {
        return Path.of(System.getProperty("java.io.tmpdir"), "SocialBridge", UUID.randomUUID().toString());
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    @Override
    public CompletableFuture<MinecraftUser> tryGetUser(UUID minecraftId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<String> get(ISocialModule module, String parameter, String defaultValue) {
        return get(module.getId(), parameter, defaultValue);
    }

    @Override
    public CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue) {
        var moduleConfig = config.getOrDefault(moduleId, null);
        if (moduleConfig == null) {
            moduleConfig = new HashMap<>();
            config.put(moduleId, moduleConfig);
        }

        var result = moduleConfig.getOrDefault(parameter, defaultValue);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Boolean> set(ISocialModule module, String parameter, String value) {
        return set(module.getId(), parameter, value);
    }

    @Override
    public CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value) {
        var moduleConfig = config.getOrDefault(moduleId, null);
        if (moduleConfig == null) {
            moduleConfig = new HashMap<>();
            config.put(moduleId, moduleConfig);
        }

        moduleConfig.put(parameter, value);
        return CompletableFuture.completedFuture(true);
    }

    private static boolean isInited = false;
    public static void Init() throws SQLException, IOException {
        if (isInited) {
            return;
        }

        var mcPlatform = new HeadlessMinecraftPlatform();
        mcPlatform.set(DefaultModule.MODULE_ID, "connectionString", "jdbc:h2:mem:account");

        SocialBridge.Init(mcPlatform);
        isInited = true;
    }

    @Override
    public Version getSocialBridgeVersion() {
        return VERSION;
    }

    @Override
    public CompletableFuture<Void> connectModule(ISocialModule module) {
        registeredModules.add(module);
        return CompletableFuture.completedFuture(null);
    }
}
